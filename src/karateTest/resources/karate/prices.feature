Feature: Prices API end to end

  Background:
    * url baseUrl
    * path '/api/v1/prices'

  Scenario: Applicable price with all contract fields
    * params { applicationDate: '2020-06-14T16:00:00', productId: 35455, brandId: 1 }
    When method get
    Then status 200
    And match response == { productId: 35455, brandId: 1, priceList: 2, startDate: '2020-06-14T15:00:00', endDate: '2020-06-14T18:30:00', price: 25.45, currency: 'EUR' }

  Scenario: No applicable price answers problem+json
    * params { applicationDate: '2019-01-01T00:00:00', productId: 35455, brandId: 1 }
    When method get
    Then status 404
    And match header Content-Type contains 'application/problem+json'
    And match response contains { status: 404, title: 'Price not found' }

  Scenario: Malformed date answers problem+json
    * params { applicationDate: 'not-a-date', productId: 35455, brandId: 1 }
    When method get
    Then status 400
    And match header Content-Type contains 'application/problem+json'
    And match response.status == 400
