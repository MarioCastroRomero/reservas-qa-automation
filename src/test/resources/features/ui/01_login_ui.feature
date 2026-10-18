# language: es
@ui @Login
Característica: Inicio de sesión desde la interfaz web
  Como persona con cuenta en la plataforma de reservas
  Quiero iniciar sesión desde la pantalla de login
  Para acceder al catálogo de negocios y gestionar mis reservas

  @Positivo
  Escenario: Inicio de sesión correcto lleva al catálogo de negocios
    Dado que existe una cuenta registrada con una clave válida
    Cuando la persona inicia sesión en la web con esas credenciales
    Entonces la web muestra el catálogo de negocios
    Y el saludo incluye el nombre "Ana"

  @Negativo
  Escenario: Clave incorrecta muestra un error y mantiene en el login
    Dado que existe una cuenta registrada con una clave válida
    Cuando la persona inicia sesión en la web con una clave incorrecta
    Entonces la web muestra el error "Email o contraseña incorrectos"
    Y la persona sigue en la pantalla de login

  @Validacion
  Escenario: Los campos vacíos se validan en el navegador sin llamar a la API
    Dado que la persona está en la pantalla de login
    Cuando envía el formulario de login sin rellenar nada
    Entonces la web marca errores en los campos del formulario

  @Seguridad
  Escenario: Una ruta protegida redirige al login si no hay sesión
    Cuando una persona sin sesión abre "/mis-reservas"
    Entonces la persona sigue en la pantalla de login
