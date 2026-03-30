How to run? 

javac -cp src:/usr/share/java/gson.jar -d out $(find src -name "*.java")
cp src/com/booking/database.json out/com/booking/database.json                                                                                                                            
java -cp out:/usr/share/java/gson.jar com.booking.Main