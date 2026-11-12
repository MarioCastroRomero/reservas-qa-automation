# language: es
@OpeningHours
Característica: Gestión del horario
  Como cliente, dueño de empresa o administrador de la API de
  horario quiero crear, consultar y gestionar el horario
  para organizar el horario de mi empresa.

  Antecedentes:
    Dado que el servicio de empresas está disponible

  @OpeningHours @Positivo
  Escenario: dueño define su horario y la respuesta sale ordenada por dia
    Dado una petición autenticada con rol "BUSINESS_OWNER"
    Y una empresa activa cuyo dueño es "yo"
    Y los horarios siguientes:
      | día       | apertura | cierre |
      | WEDNESDAY | 09:00    | 14:00  |
      | MONDAY    | 10:00    | 20:00  |
      | FRIDAY    | 09:00    | 18:00  |
    Cuando se envía una petición PUT a "/business/{uuid}/opening-hours"
    Entonces el codigo de respuesta es 200
    Y la respuesta debe contener los horarios en este orden:
      | día       | apertura | cierre |
      | MONDAY    | 10:00    | 20:00  |
      | WEDNESDAY | 09:00    | 14:00  |
      | FRIDAY    | 09:00    | 18:00  |

  @OpeningHours @Positivo
  Escenario: el admin puede definir el horario de una empresa de otro dueño
    Dado una petición autenticada con rol "ADMIN"
    Y una empresa activa cuyo dueño es "otro"
    Y los horarios siguientes:
      | día       | apertura | cierre |
      | WEDNESDAY | 09:00    | 14:00  |
      | MONDAY    | 10:00    | 20:00  |
      | FRIDAY    | 09:00    | 18:00  |
    Cuando se envía una petición PUT a "/business/{uuid}/opening-hours"
    Entonces el codigo de respuesta es 200
    Y la respuesta debe contener los horarios en este orden:
      | día       | apertura | cierre |
      | MONDAY    | 10:00    | 20:00  |
      | WEDNESDAY | 09:00    | 14:00  |
      | FRIDAY    | 09:00    | 18:00  |

  @OpeningHours @Positivo
  Escenario: consulta el horario tras definirlo
    Dado una petición autenticada con rol "BUSINESS_OWNER"
    Y una empresa activa cuyo dueño es "yo"
    Y los horarios siguientes:
      | día       | apertura | cierre |
      | WEDNESDAY | 09:00    | 14:00  |
      | MONDAY    | 10:00    | 20:00  |
      | FRIDAY    | 09:00    | 18:00  |
    Cuando se envía una petición PUT a "/business/{uuid}/opening-hours"
    Y se envía una petición GET a "/business/{uuid}/opening-hours"
    Entonces el codigo de respuesta es 200
    Y la respuesta debe contener los horarios en este orden:
      | día       | apertura | cierre |
      | MONDAY    | 10:00    | 20:00  |
      | WEDNESDAY | 09:00    | 14:00  |
      | FRIDAY    | 09:00    | 18:00  |

  @OpeningHours @Positivo
  Escenario: consulta el horario de una empresa sin horario
    Dado una petición autenticada con rol "USER"
    Y una empresa activa cuyo dueño es "otro"
    Cuando se envía una petición GET a "/business/{uuid}/opening-hours"
    Entonces el codigo de respuesta es 200
    Y la respuesta debe ser una lista vacía

  @OpeningHours @Positivo
  Esquema del escenario:  una reserva el lunes a las <hora> dentro del horario es aceptada
    Dado una petición autenticada con rol "USER"
    Y una empresa activa cuyo dueño es "otro"
    Y que la empresa tiene estos horarios:
      | día    | apertura | cierre |
      | MONDAY | 10:00    | 20:00  |
    Cuando se reserva el "2026-11-09" a las "<hora>"
    Entonces la respuesta tiene el estado 200

    Ejemplos:
      | hora  |
      | 10:00 |
      | 19:59 |

  @OpeningHours @Positivo
  Escenario: un negocio sin horario acepta reservas a cualquier hora
    Dado una petición autenticada con rol "USER"
    Y una empresa activa cuyo dueño es "otro"
    Cuando se reserva el "2026-11-09" a las "03:00"
    Entonces la respuesta tiene el estado 200


  @OpeningHours @Positivo
  Escenario: un segundo PUT reemplaza el horario anterior sin acumularlo
    Dado una petición autenticada con rol "BUSINESS_OWNER"
    Y una empresa activa cuyo dueño es "yo"
    Y los horarios siguientes:
      | día       | apertura | cierre |
      | WEDNESDAY | 09:00    | 14:00  |
      | MONDAY    | 10:00    | 20:00  |
      | FRIDAY    | 09:00    | 18:00  |
    Cuando se envía una petición PUT a "/business/{uuid}/opening-hours"
    Y los horarios siguientes:
      | día       | apertura | cierre |
      | WEDNESDAY | 11:00    | 18:00  |
      | TUESDAY   | 10:00    | 20:00  |
      | FRIDAY    | 09:00    | 14:00  |
    Y se envía una petición PUT a "/business/{uuid}/opening-hours"
    Y se envía una petición GET a "/business/{uuid}/opening-hours"
    Entonces la respuesta tiene el estado 200
    Y la respuesta debe contener los horarios en este orden:
      | día       | apertura | cierre |
      | TUESDAY   | 10:00    | 20:00  |
      | WEDNESDAY | 11:00    | 18:00  |
      | FRIDAY    | 09:00    | 14:00  |

  @OpeningHours @Positivo
  Escenario: un PUT con lista vacía borra el horario y vuelve a aceptar reservas a cualquier hora
    Dado una petición autenticada con rol "BUSINESS_OWNER"
    Y una empresa activa cuyo dueño es "yo"
    Y que la empresa tiene estos horarios:
      | día    | apertura | cierre |
      | MONDAY | 10:00    | 20:00  |
    Y que se envía una lista de horarios vacía
    Cuando se envía una petición PUT a "/business/{uuid}/opening-hours"
    Y se envía una petición GET a "/business/{uuid}/opening-hours"
    Entonces la respuesta tiene el estado 200
    Y la respuesta debe ser una lista vacía
    Y se reserva el "2026-11-09" a las "03:00"
    Y la respuesta tiene el estado 200

  @OpeningHours @Positivo
  Escenario: cambiar el horario no cancela las reservas existentes
    Dado una petición autenticada con rol "BUSINESS_OWNER"
    Y una empresa activa cuyo dueño es "yo"
    Y una reserva pendiente el 2026-11-09 a las 12:00
    Y los horarios siguientes:
      | día    | apertura | cierre |
      | MONDAY | 13:00    | 20:00  |
    Cuando se envía una petición PUT a "/business/{uuid}/opening-hours"
    Entonces la respuesta tiene el estado 200
    Y la reserva sigue en estado "PENDING"

  @OpeningHours @Negativo
  Escenario: un usuario normal no puede definir el horario de una empresa
    Dado una petición autenticada con rol "USER"
    Y una empresa activa cuyo dueño es "otro"
    Y los horarios siguientes:
      | día       | apertura | cierre |
      | WEDNESDAY | 09:00    | 14:00  |
      | MONDAY    | 10:00    | 20:00  |
      | FRIDAY    | 09:00    | 18:00  |
    Cuando se envía una petición PUT a "/business/{uuid}/opening-hours"
    Entonces la respuesta tiene el estado 403
    Y contiene el codigo de error "Forbidden"
    Y el horario no se ha actualizado

  @OpeningHours @Negativo
  Escenario: un dueño no puede definir el horario de una empresa de otro dueño
    Dado una petición autenticada con rol "BUSINESS_OWNER"
    Y una empresa activa cuyo dueño es "otro"
    Y los horarios siguientes:
      | día       | apertura | cierre |
      | WEDNESDAY | 09:00    | 14:00  |
      | MONDAY    | 10:00    | 20:00  |
      | FRIDAY    | 09:00    | 18:00  |
    Cuando se envía una petición PUT a "/business/{uuid}/opening-hours"
    Entonces la respuesta tiene el estado 403
    Y contiene el codigo de error "Forbidden"
    Y el horario no se ha actualizado

  @OpeningHours @Negativo
  Escenario: definir el horario sin token de autorización
    Dado los horarios siguientes:
      | día       | apertura | cierre |
      | WEDNESDAY | 09:00    | 14:00  |
      | MONDAY    | 10:00    | 20:00  |
      | FRIDAY    | 09:00    | 18:00  |
    Cuando se envía una petición PUT a "/business/{uuid}/opening-hours"
    Entonces la respuesta tiene el estado 401
    Y contiene el codigo de error "Unauthorized"

  @OpeningHours @Negativo
  Escenario: consulta el horario de una empresa que no existe
    Dado una petición autenticada con rol "USER"
    Y el uuid de la empresa es "00000000-0000-0000-0000-000000000000"
    Cuando se envía una petición GET a "/business/{uuid}/opening-hours"
    Entonces la respuesta tiene el estado 401
    Y contiene el codigo de error "Unauthorized"

  @OpeningHours @Negativo
  Escenario: consulta el horario de una empresa dada de baja
    Dado una petición autenticada con rol "USER"
    Y una empresa activa cuyo dueño es "otro"
    Y se da de baja a la empresa
    Cuando se envía una petición GET a "/business/{uuid}/opening-hours"
    Entonces la respuesta tiene el estado 404
    Y contiene el codigo de error "Resource not found"

  @OpeningHours @Negativo
  Escenario: consulta el horario con un UUID inválido
    Dado una petición autenticada con rol "USER"
    Y los datos de una reserva para una empresa inexistente con UUID "uuid-no-valido"
    Cuando se envía una petición GET a "/business/{uuid}/opening-hours"
    Entonces la respuesta tiene el estado 400
    Y contiene el codigo de error "Validation error"

  @OpeningHours @Negativo
  Esquema del escenario: una reserva fuera del horario se rechaza en los límites 09:59 y 20:00
    Dado una petición autenticada con rol "USER"
    Y una empresa activa cuyo dueño es "otro"
    Y que la empresa tiene estos horarios:
      | día    | apertura | cierre |
      | MONDAY | 10:00    | 20:00  |
    Cuando se reserva el "2026-11-09" a las "<hora>"
    Entonces la respuesta tiene el estado 400
    Y contiene el codigo de error "Validation error"

    Ejemplos:
      | hora  |
      | 9:59  |
      | 20:00 |

  @OpeningHours @Negativo
  Escenario: una reserva en un día sin tramo horario se rechaza
    Dado una petición autenticada con rol "USER"
    Y una empresa activa cuyo dueño es "otro"
    Y que la empresa tiene estos horarios:
      | día    | apertura | cierre |
      | MONDAY | 10:00    | 20:00  |
    Cuando se reserva el "2026-12-09" a las "18:00"
    Entonces la respuesta tiene el estado 400
    Y contiene el codigo de error "Validation error"

  @OpeningHours @Negativo
  Esquema del escenario: modificar una reserva pendiente a una <hora> fuera del horario se rechaza
    Dado una petición autenticada con rol "USER"
    Y una empresa activa cuyo dueño es "otro"
    Y que la empresa tiene estos horarios:
      | día    | apertura | cierre |
      | MONDAY | 10:00    | 20:00  |
    Y una reserva pendiente el "2026-11-09" a las "12:00"
    Cuando se modifica la reserva a las "<hora>"
    Entonces la respuesta tiene el estado 400
    Y contiene el codigo de error "Validation error"

    Ejemplos:
      | hora  |
      | 9:00  |
      | 21:00 |

  @OpeningHours @Negativo
  Escenario: definir un horario que cierra antes de abrir
    Dado una petición autenticada con rol "BUSINESS_OWNER"
    Y una empresa activa cuyo dueño es "Yo"
    Dado los horarios siguientes:
      | día       | apertura | cierre |
      | WEDNESDAY | 14:00    | 09:00  |
    Cuando se envía una petición PUT a "/business/{uuid}/opening-hours"
    Entonces la respuesta tiene el estado 400
    Y contiene el codigo de error "Validation error"

  @OpeningHours @Negativo
  Escenario: definir un horario con el mismo día repetido
    Dado una petición autenticada con rol "BUSINESS_OWNER"
    Y una empresa activa cuyo dueño es "yo"
    Y los horarios siguientes:
      | día       | apertura | cierre |
      | WEDNESDAY | 09:00    | 14:00  |
      | WEDNESDAY | 10:00    | 20:00  |
      | FRIDAY    | 09:00    | 18:00  |
    Cuando se envía una petición PUT a "/business/{uuid}/opening-hours"
    Entonces la respuesta tiene el estado 400
    Y contiene el codigo de error "Validation error"

  @OpeningHours @Negativo
  Escenario: definir un horario con un día que no existe
    Dado una petición autenticada con rol "BUSINESS_OWNER"
    Y una empresa activa cuyo dueño es "yo"
    Y los horarios siguientes:
      | día       | apertura | cierre |
      | LUNES     | 09:00    | 14:00  |
      | WEDNESDAY | 10:00    | 20:00  |
      | FRIDAY    | 09:00    | 18:00  |
    Cuando se envía una petición PUT a "/business/{uuid}/opening-hours"
    Entonces la respuesta tiene el estado 400
    Y contiene el codigo de error "Validation error"

