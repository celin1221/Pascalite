// =================================================
// Exemplo 4: Ponto Extra - Registros e Enumerações
// =================================================
PROGRAM ExemploBonus;

TYPE
   Pessoa = RECORD
      nome : STRING;
      idade : INTEGER;
      salario : REAL;
   END;

   DiasSemana = ENUM OF (Segunda, Terca, Quarta, Quinta, Sexta, Sabado, Domingo);

VAR
   funcionario : Pessoa;
   diaTrabalho : DiasSemana;

BEGIN
   diaTrabalho := Segunda;
   funcionario := 'Registrado';
END.
