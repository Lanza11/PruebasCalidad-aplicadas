Feature: Listar Solicitudes

  Background:
    * url baseUrl

  Scenario: GET /Listar - responde con lista
    Given path '/Listar'
    When method GET
    Then status 200
    And match response == '#array'

  Scenario: GET /Listar - cada elemento tiene estructura correcta
    Given path '/Solicitud/Crear'
    And request { usuario: 'Test Listar', tipo: 'INCIDENTE', prioridadManual: 2, fechaCreacion: '2024-04-01T10:00:00' }
    When method POST
    Then status 200

    Given path '/Listar'
    When method GET
    Then status 200
    And match each response == { id: '#number', tipo: '#string', prioridadManual: '#number', fechaCreacion: '#string', usuario: '#string' }

  Scenario: GET /Listar - tipos son valores válidos del enum
    Given path '/Solicitud/Crear'
    And request { usuario: 'Test Enum', tipo: 'CONSULTA', prioridadManual: 1, fechaCreacion: '2024-02-01T10:00:00' }
    When method POST
    Then status 200

    Given path '/Listar'
    When method GET
    Then status 200
    * def tipos = $response[*].tipo
    And match each tipos == '#regex (INCIDENTE|REQUERIMIENTO|CONSULTA)'

  Scenario: GET /Listar - prioridad entre 1 y 5
    Given path '/Listar'
    When method GET
    Then status 200
    * def prioridades = $response[*].prioridadManual
    And match each prioridades == '#? _ >= 1 && _ <= 5'

  Scenario: GET /Listar/Priorizado - responde con lista
    Given path '/Listar/Priorizado'
    When method GET
    Then status 200
    And match response == '#array'

  Scenario: GET /Listar/Priorizado - cada elemento tiene solicitud y score
    Given path '/Solicitud/Crear'
    And request { usuario: 'Test Score', tipo: 'INCIDENTE', prioridadManual: 5, fechaCreacion: '2023-01-01T00:00:00' }
    When method POST
    Then status 200

    Given path '/Listar/Priorizado'
    When method GET
    Then status 200
    And match each response == { solicitud: { id: '#number', tipo: '#string', prioridadManual: '#number', fechaCreacion: '#string', usuario: '#string' }, score: '#number' }

  Scenario: GET /Listar/Priorizado - INCIDENTE tiene mayor score que CONSULTA con misma prioridad
    Given path '/Solicitud/Crear'
    And request { usuario: 'Incidente Comp', tipo: 'INCIDENTE', prioridadManual: 3, fechaCreacion: '2024-01-15T10:00:00' }
    When method POST
    Then status 200
    * def idIncidente = response.id

    Given path '/Solicitud/Crear'
    And request { usuario: 'Consulta Comp', tipo: 'CONSULTA', prioridadManual: 3, fechaCreacion: '2024-01-15T10:00:00' }
    When method POST
    Then status 200
    * def idConsulta = response.id

    Given path '/Listar/Priorizado'
    When method GET
    Then status 200
    * def lista = response
    * def scoreI = karate.jsonPath(lista, "$[?(@.solicitud.id == " + idIncidente + ")].score")[0]
    * def scoreC = karate.jsonPath(lista, "$[?(@.solicitud.id == " + idConsulta + ")].score")[0]
    And assert scoreI > scoreC