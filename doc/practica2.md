# Documentación Técnica - Práctica 2 (v1.1.0)

## 1. Introducción

En la versión 1.1.0 hemos implementado varias funcionalidades centradas en la gestión de usuarios, la seguridad y la navegación. Los cambios principales incluyen una barra de menú dinámica, un listado de usuarios registrados y un rol de administrador que puede bloquear o habilitar el acceso a otros usuarios. Cada funcionalidad se ha desarrollado en su propio *issue* con una rama independiente y se ha integrado en `main` mediante un *pull request*.

## 2. Nuevas clases y métodos

Para implementar estas funcionalidades, hemos añadido y modificado los siguientes elementos:

- **`UsuarioController` (nueva clase):** Centraliza las acciones del administrador. Incluye los métodos `listadoUsuarios()` (`GET /registrados`), `descripcionUsuario()` (`GET /registrados/{id}`), `cambiarBloqueo()` (`POST /registrados/{id}/bloqueo`) y el método privado `comprobarAdmin()`.
- **`HomeController` y `TareaController`:** Los métodos que devuelven vistas (como `about()` o `formEditaTarea()`) ahora inyectan la variable `usuario` en el modelo para mostrar la barra de navegación correcta.
- **`LoginController`:** Se ha modificado para redirigir al administrador a `/registrados` al hacer login, se ha añadido `existeAdmin` en el registro, y se muestra un error específico si el usuario está bloqueado.
- **`UsuarioService`:** Se han añadido `allUsuarios()`, `existeAdmin()`, `cambiarBloqueo()`, la regla de un solo administrador en `registrar()` y el estado `USER_BLOCKED` en el login.
- **`UsuarioRepository`:** Añade `existsByAdminTrue()`, una *derived query* de Spring Data.
- **Entidades y DTOs:** `Usuario` y `UsuarioData` tienen los atributos booleanos `admin` y `bloqueado`. `RegistroData` solo incluye el atributo `admin`.
- **Excepciones:** Se han creado `UsuarioNotFoundException` (404) y `UsuarioNoAutorizadoException` (401).

## 3. Plantillas Thymeleaf

La presentación del proyecto se ha organizado mejor:

- **Nuevas vistas:** Se han creado `listaUsuarios.html` y `descripcionUsuario.html`.
- **Vistas modificadas:**
  - `fragments.html`: contiene los fragmentos `navbar` y `navbarNoLogeado`.
  - `about.html`: usa `th:block` para mostrar condicionalmente una barra u otra.
  - `formRegistro.html`: incluye el checkbox de administrador.
  - `listaTareas.html`, `formNuevaTarea.html` y `formEditarTarea.html`: ahora incluyen la barra de navegación superior.

## 4. Tests

El número de pruebas ha pasado de 35 a 61. Hemos usado tres enfoques:

- **Repository:** `UsuarioTest`.
- **Service:** `UsuarioServiceTest` y `TareaServiceTest`.
- **Web:** `UsuariosRegistradosWebTest`, `AcercaDeWebTest`, `UsuarioWebTest` y `TareaWebTest`.

Para los tests web usamos `MockMvc` junto con `@Sql(scripts = "/clean-db.sql")`. También utilizamos `@MockBean` sobre `ManagerUserSession` para simular qué usuario está logeado sin tener que pasar por el formulario de login en cada prueba.

Un aprendizaje importante fue el comportamiento de Mockito con tipos envoltorio (`Long`). Al devolver `0L` por defecto en lugar de `null`, el test de Acerca de sin login (`getAboutSinLoginMuestraLoginYRegistro`) pasaba por casualidad, mientras que el test de seguridad `listadoUsuariosSinLoginDevuelve401` fallaba lanzando un `NullPointerException`. Lo solucionamos forzando explícitamente `thenReturn(null)` para simular la falta de sesión.

## 5. Código interesante explicado

### Programación defensiva en `comprobarAdmin()`

```java
private UsuarioData comprobarAdmin() {
    Long idUsuario = managerUserSession.usuarioLogeado();
    if (idUsuario == null) {
        throw new UsuarioNoLogeadoException();
    }

    UsuarioData usuario = usuarioService.findById(idUsuario);

    // Comprobamos si el usuario no existe en la BD
    if (usuario == null) {
        throw new UsuarioNoLogeadoException();
    }

    if (!usuario.isAdmin()) {
        throw new UsuarioNoAutorizadoException();
    }
    return usuario;
}
```

Este método incluye programación defensiva. No basta con comprobar si hay una sesión activa (`idUsuario == null`); también hay que verificar si el usuario realmente existe en la base de datos. Por ejemplo, si un usuario es borrado de la base de datos mientras tiene la sesión abierta, o si en los tests Mockito devuelve `0L`, `findById` devolverá `null`. Si no comprobáramos esto e intentáramos acceder a `usuario.isAdmin()`, saltaría un `NullPointerException`. Comprobando el `null`, evitamos el error 500 y devolvemos un error 401 correctamente.

### Patrón Post/Redirect/Get en el bloqueo de usuarios

```java
@PostMapping("/registrados/{id}/bloqueo")
public String cambiarBloqueo(@PathVariable(value="id") Long idUsuario) {
    comprobarAdmin();
    usuarioService.cambiarBloqueo(idUsuario);
    return "redirect:/registrados";
}
```

Para bloquear y habilitar usuarios usamos el patrón Post/Redirect/Get (PRG). En la vista, en lugar de un enlace `<a>` (que haría una petición GET), usamos un pequeño formulario con un único botón que envía la petición por POST. Tras modificar el estado, el método devuelve un `redirect` en lugar de pintar la vista directamente. Esto evita que, si recargamos la página (F5), el navegador repita la petición POST y vuelva a cambiar el estado del usuario por error.

## 6. Otros cambios

- **Docker:** Actualizamos la imagen base del `Dockerfile` (issue #3).
- **Mensajes de error:** Añadimos `server.error.include-message=always` en `application.properties` para que Spring Boot muestre los mensajes de error (como "No tienes permisos") en la pantalla, en lugar de mostrar solo la palabra "Unauthorized".
