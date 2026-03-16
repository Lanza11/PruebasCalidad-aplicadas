Feature: Crear Solicitud - POST /Solicitud/Crear

  Background:
    * url baseUrl
    * path '/Solicitud/Crear'

  # Caso exitoso básico
  Scenario: Crear solicitud INCIDENTE válida
    Given request { usuario: 'Juan Perez', tipo: 'INCIDENTE', prioridadManual: 1, fechaCreacion: '2024-01-15T10:00:00' }
    When method POST
    Then status 200
    And match response.id == '#number'
    And match response.tipo == 'INCIDENTE'
    And match response.prioridadManual == 1

  # Verificar que los 3 tipos funcionan
  Scenario: Crear solicitud REQUERIMIENTO válida
    Given request { usuario: 'Maria Garcia', tipo: 'REQUERIMIENTO', prioridadManual: 5, fechaCreacion: '2024-03-01T08:30:00' }
    When method POST
    Then status 200
    And match response.tipo == 'REQUERIMIENTO'

  Scenario: Crear solicitud CONSULTA válida
    Given request { usuario: 'Carlos Lopez', tipo: 'CONSULTA', prioridadManual: 1, fechaCreacion: '2024-06-10T14:00:00' }
    When method POST
    Then status 200
    And match response.tipo == 'CONSULTA'

  # Sin fecha - verifica autoasignación
  Scenario: Crear solicitud sin fechaCreacion
    Given request { usuario: 'Ana Torres', tipo: 'INCIDENTE', prioridadManual: 3 }
    When method POST
    Then status 200
    And match response.fechaCreacion == '#string'

  # Validación usuario
  Scenario: Error 400 - usuario vacío
    Given request { usuario: '', tipo: 'INCIDENTE', prioridadManual: 1, fechaCreacion: '2024-01-01T00:00:00' }
    When method POST
    Then status 400
    And match response.validationErrors.usuario == '#string'

  # Validación tipo
  Scenario: Error 400 - tipo inválido
    Given request { usuario: 'Juan Perez', tipo: 'URGENTE', prioridadManual: 1, fechaCreacion: '2024-01-01T00:00:00' }
    When method POST
    Then status 400

  # Validación prioridad fuera de rango
  Scenario: Error 400 - prioridad fuera de rango
    Given request { usuario: 'Juan Perez', tipo: 'INCIDENTE', prioridadManual: 6, fechaCreacion: '2024-01-01T00:00:00' }
    When method POST
    Then status 400
    And match response.validationErrors.prioridadManual == '#string'

  # Validación fecha futura
  Scenario: Error 400 - fecha en el futuro
    Given request { usuario: 'Juan Perez', tipo: 'INCIDENTE', prioridadManual: 3, fechaCreacion: '2099-12-31T23:59:59' }
    When method POST
    Then status 400
    And match response.validationErrors.fechaCreacion == '#string'

  # Body vacío
  Scenario: Error 400 - body vacío
    Given request {}
    When method POST
    Then status 400

  # Múltiples errores a la vez
  Scenario: Error 400 - múltiples campos inválidos
    Given request { usuario: '', prioridadManual: 10 }
    When method POST
    Then status 400
    And match response.validationErrors.usuario == '#string'
    And match response.validationErrors.prioridadManual == '#string'