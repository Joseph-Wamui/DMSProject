export const environment = {
  production: true,
  baseUrl: "${window.location.protocol}//${window.location.hostname}/api",  // This will automatically use the ALB hostname
  baseUrlSelfservice: "http://52.15.152.26:9099",
  testUrl: "${window.location.protocol}//${window.location.hostname}/api"  // This will automatically use the ALB hostname
};
