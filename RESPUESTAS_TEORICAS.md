# Evaluación Final — Parte 4: Respuestas Teóricas

## P1 — ¿Por qué `SecurityConfig` no está en `domain` ni `application`?

`SecurityConfig` depende directamente de clases de Spring Security (`HttpSecurity`,
`SecurityFilterChain`, `JwtAuthenticationConverter`), que son tecnologías de
infraestructura. El dominio y la capa de aplicación no deben conocer ningún
framework externo — su única responsabilidad es expresar reglas de negocio y
orquestar casos de uso. Colocar `SecurityConfig` en `domain` o `application`
violaría la regla de dependencias de la arquitectura hexagonal: las capas internas
no pueden depender de las externas. Por eso vive en `infraestructure`, como un
adaptador de entrada más (inbound adapter), al mismo nivel que el `OrderController`.

---

## P2 — JWT válido pero sin roles de Keycloak: ¿puede acceder a `GET /api/orders/{id}`?

**Sí, puede acceder.** La regla que protege el nuevo endpoint es:

```java
.requestMatchers(HttpMethod.GET, "/api/orders/**").authenticated()
```

`authenticated()` solo exige que el token JWT sea válido (firma correcta, no expirado,
issuer reconocido). No requiere ningún rol específico. Si el token está bien formado y
fue emitido por Keycloak, Spring Security lo acepta aunque `realm_access.roles` esté
vacío. La diferencia con los endpoints de escritura es que esos usan `hasRole("ADMIN")`
o `hasAnyRole("ADMIN","USER")`, que sí requieren un rol concreto extraído por
`KeycloakRoleConverter`.

---

## P3 — ¿Qué hace `KeycloakRoleConverter` y qué pasaría si no existiera?

`KeycloakRoleConverter` implementa `Converter<Jwt, Collection<GrantedAuthority>>` y
extrae la lista de roles del claim `realm_access.roles` del token JWT de Keycloak,
añadiéndoles el prefijo `ROLE_` que Spring Security exige internamente para que
`hasRole("ADMIN")` funcione (busca `ROLE_ADMIN`).

Sin él, Spring Security usaría su conversor por defecto, que lee el claim `scope` o
`scp` del JWT — campos estándar de OAuth2 que Keycloak no puebla con roles de realm.
El resultado sería que ningún usuario tendría roles asignados, todos los endpoints
protegidos con `hasRole(...)` devolverían **403 Forbidden**, y la autorización
basada en roles quedaría completamente rota aunque el token fuese válido.

---

## P4 — Diferencia entre 401 Unauthorized y 403 Forbidden

| Código | Significado | Causa |
|--------|-------------|-------|
| **401 Unauthorized** | No autenticado — el servidor no sabe quién eres | Falta el token JWT, el token expiró, la firma es inválida, o el issuer no coincide con el configurado en `application.yml` |
| **403 Forbidden** | Autenticado pero sin permisos — el servidor sabe quién eres, pero no tienes acceso | Token JWT válido pero el usuario no tiene el rol requerido por el endpoint |

**Ejemplo concreto en este proyecto:**

- **401**: un cliente llama a `POST /api/orders` sin cabecera `Authorization`. Spring
  Security rechaza la petición antes de evaluar roles porque no puede validar ningún
  token → responde **401**.

- **403**: un usuario autenticado con rol `USER` llama a `POST /api/orders` (crear
  orden). El token es válido y `KeycloakRoleConverter` extrae `ROLE_USER`, pero la
  regla exige `ROLE_ADMIN`. Spring Security reconoce al usuario pero le deniega el
  acceso → responde **403**.
