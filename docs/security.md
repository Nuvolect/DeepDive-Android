DD Security  
-----------  

Security Dependencies  
---------------------  

DD uses the Guardian Project IOCipher encrypted filesystem which is based on SQLCipher
DD also uses Java's standard security packages


Data in Place  
-------------  

1. The Crypto volume 32 character password is randomly generated using Math.random()
2. It is encrypted with the Android keystore and stored in the Apps private data area


Data in Motion  
--------------  

1. DD uses a self signed security certificate for https communications
2. The certificate cannot be validated by the browser, hence it generates nasty warnings


Vulnerabilities  
---------------  

1. Math.random() can be predicted, it is used to create the initial database password
2. The static encryption key can be derived if the device ID is captured
3. Certain memory locations hold sensitive information that may be exposed in virtual memory


Security Improvements  
---------------------  

1. Clear memory holding sensitive information after use


