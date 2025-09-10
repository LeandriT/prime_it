Feature: Users API

  Background:
    * url baseUrl
    * configure headers = { 'Content-Type': 'application/json' }

  Scenario: listar usuarios (paginado)
    Given path 'api/users'
    And param page = 0
    And param size = 10
    When method get
    Then status 200
    And match response == '#present'

  Scenario: crear, leer, actualizar parcialmente y borrar
    # crear
    * def payload =
      """
      {
        "name": "John Smith",
        "email": "john.smith@test.dev",
        "password": "S3cret!",
        "phones": [ { "number": "1234567", "city_code": "2", "country_code": "56" } ]
      }
      """
    Given path 'api/users'
    And request payload
    When method post
    Then status 201
    # Mantener estas validaciones, ya que validan el contenido de la respuesta de creación
    And match response.uuid == '#string'
    And match response.name == 'John Smith'
    And match response.email == 'john.smith@test.dev'
    * def uid = response.uuid

    # show
    Given path 'api/users', uid
    When method get
    Then status 200
    # **Solución**: Combina todas las validaciones en un solo 'match'
    And match response ==
      """
      {
        "uuid": "#string",
        "name": "#string",
        "email": "#string",
        "created_at": "#string",
        "updated_at": "#string",
        "last_login": "#string",
        "token": "#string",
        "phones": "#array",
        "is_active": "#boolean"
      }
      """
    # Para validar campos específicos, puedes usar la notación 'contains' o validarlos en la misma estructura.
    And match response.uuid == uid
    And match response.email == 'john.smith@test.dev'
    And match each response.phones[*] ==
      """
      {
        "uuid": "#string",
        "number": "#string",
        "city_code": "#string",
        "country_code": "#string"
      }
      """

    # patch (active=false)
    * def patchReq = { "active": false }
    Given path 'api/users', uid
    And request patchReq
    When method patch
    Then status 200
    And match response.uuid == uid
    And match response.is_active == false

    # put (update nombre y phone)
    * def putReq =
      """
      {
        "name": "John Updated",
        "email": "john.smith@test.dev",
        "password": "S3cret!",
        "phones": [ { "number": "9999999", "city_code": 9, "country_code": 56 } ]
      }
      """
    Given path 'api/users', uid
    And request putReq
    When method put
    Then status 204

    # verify after put
    Given path 'api/users', uid
    When method get
    Then status 200
    And match response.name == 'John Updated'
    And match response.phones[0].number == '9999999'

    # delete
    Given path 'api/users', uid
    When method delete
    Then status 204

    # verify 404
    Given path 'api/users', uid
    When method get
    Then status 404