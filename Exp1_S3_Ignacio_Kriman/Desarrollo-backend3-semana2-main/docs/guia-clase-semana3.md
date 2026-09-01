Guía rápida de clase — Semana 2 (Banco XYZ)
1. Partimos por el problema
El Banco XYZ recibe múltiples archivos operacionales y necesita procesarlos de manera eficiente y segura.

2. Mostramos los archivos de entrada
src/main/resources/input/transacciones.csv

src/main/resources/input/intereses.csv

src/main/resources/input/cuentas_anuales.csv
Hay registros correctos y registros malos para demostrar el manejo de errores.

3. Explicamos el Job
bancoXyzJob es el proceso completo que encadena secuencialmente los tres flujos financieros.

4. Explicamos los Steps
transaccionesStep

interesesStep

estadosCuentaStep
Cada uno lee, procesa y escribe su respectivo dominio.

5. Explicamos chunks
chunk(5) significa que Spring Batch procesa los registros en bloques de a cinco por transacción.

6. Explicamos paralelismo
ThreadPoolTaskExecutor configurado con 3 hilos permite que varios chunks se procesen al mismo tiempo.

7. Explicamos errores
Los datos con formatos incorrectos o campos vacíos se saltan automáticamente con BancoSkipPolicy.

8. Revisamos salidas
output/transacciones_procesadas.csv

output/intereses_procesados.csv

output/estados_cuenta_procesados.csv

output/errores_banco.csv