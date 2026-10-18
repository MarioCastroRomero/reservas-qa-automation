# language: es
@ui @wip @Reservas
Característica: Reservas desde la interfaz web
  Como persona autenticada
  Quiero reservar en un negocio y gestionar mis reservas
  Para organizar mis citas

  Antecedentes:
    Dado que hay una persona con sesión iniciada en la web
    Y que otra persona es dueña del negocio "Peluquería Lola"

  @Positivo
  Escenario: Reservar desde el detalle del negocio
    Cuando la persona reserva en "Peluquería Lola" para 2 personas dentro de 3 días
    Entonces "Mis reservas" muestra una reserva de "Peluquería Lola" en estado "Pendiente"

  @Validacion
  Escenario: No se puede reservar en una fecha pasada
    Cuando la persona intenta reservar en "Peluquería Lola" con una fecha pasada
    Entonces el formulario de reserva muestra un error en "Fecha y hora"

  @Positivo
  Escenario: Cancelar una reserva pendiente
    Dado que la persona tiene una reserva pendiente en "Peluquería Lola"
    Cuando la persona cancela esa reserva desde "Mis reservas"
    Entonces "Mis reservas" muestra una reserva de "Peluquería Lola" en estado "Cancelada"

  @Positivo
  Escenario: El filtro por pestañas oculta las reservas de otro estado
    Dado que la persona tiene una reserva pendiente en "Peluquería Lola"
    Cuando la persona filtra "Mis reservas" por el estado "Cancelada"
    Entonces "Mis reservas" no muestra ninguna reserva de "Peluquería Lola"
