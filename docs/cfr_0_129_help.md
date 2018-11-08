Example:
java -jar cfr_0_129.jar classes.jar --outputdir countercloud

java -jar cfr_0_129.jar --help
CFR 0_129

   --aexagg                         (boolean) 
   --aggressivesizethreshold        (int >= 0) default: 15000
   --allowcorrecting                (boolean)  default: true
   --analyseas                      (One of [JAR, WAR, CLASS]) 
   --arrayiter                      (boolean)  default: true if class file from version 49.0 (Java 5) or greater
   --caseinsensitivefs              (boolean)  default: false
   --clobber                        (boolean) 
   --collectioniter                 (boolean)  default: true if class file from version 49.0 (Java 5) or greater
   --commentmonitors                (boolean)  default: false
   --comments                       (boolean)  default: true
   --decodeenumswitch               (boolean)  default: true if class file from version 49.0 (Java 5) or greater
   --decodefinally                  (boolean)  default: true
   --decodelambdas                  (boolean)  default: true if class file from version 52.0 (Java 8) or greater
   --decodestringswitch             (boolean)  default: true if class file from version 51.0 (Java 7) or greater
   --dumpclasspath                  (boolean)  default: false
   --eclipse                        (boolean)  default: true
   --elidescala                     (boolean)  default: false
   --extraclasspath                 (string) 
   --forcecondpropagate             (boolean) 
   --forceexceptionprune            (boolean) 
   --forcereturningifs              (boolean) 
   --forcetopsort                   (boolean) 
   --forcetopsortaggress            (boolean) 
   --forloopaggcapture              (boolean) 
   --hidebridgemethods              (boolean)  default: true
   --hidelangimports                (boolean)  default: true
   --hidelongstrings                (boolean)  default: false
   --hideutf                        (boolean)  default: true
   --ignoreexceptions               (boolean)  default: false
   --innerclasses                   (boolean)  default: true
   --j14classobj                    (boolean)  default: false if class file from version 49.0 (Java 5) or greater
   --jarfilter                      (string) 
   --labelledblocks                 (boolean)  default: true
   --lenient                        (boolean)  default: false
   --liftconstructorinit            (boolean)  default: true
   --outputdir                      (string) 
   --outputpath                     (string) 
   --override                       (boolean)  default: true if class file from version 50.0 (Java 6) or greater
   --pullcodecase                   (boolean)  default: false
   --recover                        (boolean)  default: true
   --recovertypeclash               (boolean) 
   --recovertypehints               (boolean) 
   --relinkconststring              (boolean)  default: true
   --removebadgenerics              (boolean)  default: true
   --removeboilerplate              (boolean)  default: true
   --removedeadmethods              (boolean)  default: true
   --removeinnerclasssynthetics     (boolean)  default: true
   --rename                         (boolean)  default: false
   --renamedupmembers              
   --renameenumidents              
   --renameillegalidents           
   --renamesmallmembers             (int >= 0)  default: 0
   --showinferrable                 (boolean)  default: false if class file from version 51.0 (Java 7) or greater
   --showops                        (int >= 0)  default: 0
   --showversion                    (boolean)  default: true
   --silent                         (boolean)  default: false
   --stringbuffer                   (boolean)  default: false if class file from version 49.0 (Java 5) or greater
   --stringbuilder                  (boolean)  default: true if class file from version 49.0 (Java 5) or greater
   --sugarasserts                   (boolean)  default: true
   --sugarboxing                    (boolean)  default: true
   --sugarenums                     (boolean)  default: true if class file from version 49.0 (Java 5) or greater
   --tidymonitors                   (boolean)  default: true
   --tryresources                   (boolean)  default: true if class file from version 51.0 (Java 7) or greater
   --usenametable                   (boolean)  default: true
   --help                           (string) 

Please specify '--help optionname' for specifics, eg
   --help pullcodecase