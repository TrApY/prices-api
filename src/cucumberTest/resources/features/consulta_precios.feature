# language: es
Característica: Consulta del precio aplicable a un producto
  Como sistema de comercio electrónico
  quiero conocer la tarifa y el precio que aplican a un producto de una cadena en una fecha
  para mostrar el precio de venta correcto

  Esquema del escenario: La tarifa vigente de mayor prioridad determina el precio
    Cuando consulto el precio del producto 35455 de la cadena 1 a las "<fecha>"
    Entonces se aplica la tarifa <tarifa> con precio "<precio>" EUR

    Ejemplos:
      | fecha               | tarifa | precio |
      | 2020-06-14T10:00:00 | 1      | 35.50  |
      | 2020-06-14T16:00:00 | 2      | 25.45  |
      | 2020-06-14T21:00:00 | 1      | 35.50  |
      | 2020-06-15T10:00:00 | 3      | 30.50  |
      | 2020-06-16T21:00:00 | 4      | 38.95  |

  Escenario: Sin tarifa vigente no hay precio que aplicar
    Cuando consulto el precio del producto 35455 de la cadena 1 a las "2019-01-01T00:00:00"
    Entonces la respuesta es un error 404 en formato problem json
