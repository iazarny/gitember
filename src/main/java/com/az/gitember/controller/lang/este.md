# MEC Swagger

API description
- Documentation
- Modelling
- Stub and client generation
- Mock server generation

# New Features!

- New features go first.
- The rest of API must migrate here

# Preconditions
Swagger codegen cli v2 should be installed
On macos use
```
brew install swagger-codegen@2
```
See more: https://github.com/swagger-api/swagger-codegen#prerequisites

# Generate
Use ```update_interfaces``` to update api

# Run
Use ```launch``` to get spring in this console

# Cleanup and run
!! Use ```generate_sceletone``` when you want to generate everything. This will rewrite implementations!

# ~~Compile~~

```
mvn package 
```

# ~~Run~~
```
java -jar swagger-spring-1.0.0.jar
```

Request priority

* https://api.main.mejsh.com/proxy/api/devices?limit=1000  igor  done
* https://api.main.mejsh.com/proxy//api/devices/{DeviceId}   igor done


https://api.main.mejsh.com/proxy/api/devices/status - max

https://api.main.mejsh.com/mec-authentication-service/api/auth - max

https://api.main.mejsh.com/proxy/api/user/iam - max

GET https://api.main.mejsh.com/proxy/api/mobile/home - Max

Routines requests

* https://api.main.mejsh.com/proxy/api/actionRequests  - igor done  . Some type name changes ActionRequest -> ActionResponse , ActionRequestResult -> ActionResponseResult

* PUT https://api.main.mejsh.com/proxy/api/devices/875 - igor done

* POST https://api.main.mejsh.com/proxy/api/favorites/device - igor in progress

GET https://api.main.mejsh.com/proxy/api/manufacturer/types - Max

POST https://api.main.mejsh.com/proxy/api/manufacturer

https://api.main.mejsh.com/mec-content-service/api/rooms - Max

POST https://api.main.mejsh.com/proxy/api/logs/{unit} - max

https://api.main.mejsh.com/mec-content-service/api/content/*

