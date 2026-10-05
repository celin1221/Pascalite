PROGRAM CalculoFatorial;
{ Algoritmo clássico de Fatorial com função e laço for }
VAR
   numero, resultado, i : INTEGER;

FUNCTION Fatorial(n : INTEGER) : INTEGER;
VAR
   res : INTEGER;
BEGIN
   IF n <= 1 THEN
      res := 1
   ELSE
      res := n * Fatorial(n - 1);
END;

BEGIN
   numero := 5;
   resultado := 1;
   
   FOR i := 1 TO numero DO
      resultado := resultado * i;
END.
