# language: es
@ui @Registro
Característica: Registro de personas desde la interfaz web
  Como persona sin cuenta
  Quiero crearme una cuenta desde el formulario de registro
  Para empezar a reservar en los negocios

  @Positivo
  Escenario: Registro correcto lleva al catálogo de negocios
    Dado que la persona está en la pantalla de registro
    Cuando rellena el registro con datos válidos y un correo nuevo
    Entonces la web muestra el catálogo de negocios

  @Validacion
  Escenario: Campos obligatorios vacíos muestran errores
    Dado que la persona está en la pantalla de registro
    Cuando envía el formulario de registro sin rellenar nada
    Entonces la web marca errores en los campos del formulario

  @Validacion
  Esquema del escenario: La política de contraseña se valida en el formulario
    Dado que la persona está en la pantalla de registro
    Cuando rellena el registro con la clave "<clave>"
    Entonces el campo "Contraseña" muestra un error

    Ejemplos:
      | clave        |
      | corta1!      |
      | sinmayuscula1! |
      | SinNumero!!  |
      | SinSimbolo12 |

  @Validacion
  Escenario: Las contraseñas que no coinciden se rechazan
    Dado que la persona está en la pantalla de registro
    Cuando rellena el registro con claves distintas en "Contraseña" y "Repite la contraseña"
    Entonces el campo "Repite la contraseña" muestra un error

  @Negativo
  Escenario: Un correo ya registrado muestra el error de la API
    Dado que existe una cuenta registrada con una clave válida
    Y que la persona está en la pantalla de registro
    Cuando la persona intenta registrarse con el correo de esa cuenta
    Entonces el campo "Email" muestra el error "Ya existe una cuenta con ese email"
