function fn() {
  var baseUrl = karate.properties['karate.baseUrl'] || 'http://localhost:8080';
  karate.log('>> karate.baseUrl =', baseUrl);
  return { baseUrl: baseUrl };
}