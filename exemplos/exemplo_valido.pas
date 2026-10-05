// ==========================================
// Exemplo 1: Programa Válido do Dialeto PascaLite
// Conforme a gramática do Anexo I
// ==========================================
PROGRAM ExemploValido;

{ Declarações de Variáveis e Constantes }
VAR
   contador, limite : INTEGER;
   media : REAL;
   letra : CHAR;
   mensagem : STRING;

CONST
   MAX_ITERACOES : INTEGER = 100;
   PI = 3.14159;
   TITULO = 'Compilador PascaLite';

{ Procedimento de Teste }
PROCEDURE ImprimeResult(valor : INTEGER);
VAR
   status : STRING;
BEGIN
   IF valor >= 50 THEN
      status := 'Aprovado'
   ELSE
      status := 'Reprovado';
END;

BEGIN
   contador := 0;
   limite := 10;
   media := 8.75;
   mensagem := 'Bem-vindo ao dialeto Pascal!';

   WHILE contador < limite DO
      contador := contador + 1;

   REPEAT
      contador := contador - 1;
   UNTIL contador = 0;

   (* Chamada de procedimento e fim do programa *)
   ImprimeResult(limite);
END.
