# language: es
Característica: Consulta de Usuarios (/user)
  Como cliente o administrador de la API de reservas
  Quiero consultar los datos de un usuario
  Para gestionar los perfiles de la plataforma

  Antecedentes:
    Dado que el servicio de usuarios está disponible

  @User @Consulta @Positivo
  Escenario: Consulta exitosa de un usuario por UUID con token válido
    Dado un usuario existente con UUID conocido
    Y una petición autenticada con un token JWT válido
    Cuando se envía una petición GET a "/user/{uuid}" con ese UUID
    Entonces la respuesta debe tener el código de estado 200
    Y la respuesta debe contener los datos del usuario: uuid, nombre, apellidos, correo y fecha de nacimiento
    Y la respuesta no debe incluir la contraseña

  @User @Consulta @Seguridad @Negativo
  Escenario: Consulta de un usuario sin token de autorización
    Dado un usuario existente con UUID conocido
    Cuando se envía una petición GET a "/user/{uuid}" sin cabecera "Authorization"
    Entonces la respuesta debe tener el código de estado 401
    Y la respuesta no debe incluir datos del usuario
    Y la respuesta debe dar error de autenticacion

  @User @Consulta @NotFound @Negativo
  Escenario: Consulta de un usuario inexistente por UUID
    Dado un UUID "00000000-0000-0000-0000-000000000000" que no corresponde a ningún usuario
    Y una petición autenticada con un token JWT válido
    Cuando se envía una petición GET a "/user/{uuid}" con ese UUID
    Entonces la respuesta debe tener el código de estado 404
    Y el mensaje de error debe indicar "Resource not found"

  @User @Consulta @Validacion @Negativo
  Escenario: Consulta de un usuario con formato de UUID inválido
    Dado un UUID "uuid-no-valido-123" que no corresponde a ningún usuario
    Y una petición autenticada con un token JWT válido
    Cuando se envía una petición GET a "/user/{uuid}" con ese UUID
    Entonces la respuesta debe tener el código de estado 400
    Y el mensaje de error debe indicar "Validation error"
