package mjcompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.StringCharacterIterator;

/**
 *
 * @author Victoria Moraes
 */
public class Scanner
{
    private static String input;
    StringCharacterIterator inputIt;
    private SymbolTable st;
    int lineNumber;
    private int begin,end;

    public Scanner(SymbolTable globalST, String inputFileName)
    {
        File inputFile = new File(inputFileName);
        st = globalST;

        try
        {
            FileReader fr = new FileReader(inputFile);

            int size = (int)inputFile.length();
            char[] buffer = new char[size];

            fr.read(buffer, 0, size);

            input = new String(buffer);

            inputIt = new StringCharacterIterator(input);

            lineNumber = 1;
            begin = 0;
            end = 0;
        } 
        catch(FileNotFoundException e)
        {
            System.err.println("Arquivo não encontrado");
        }
        catch(IOException e)
        {
            System.err.println("Erro na leitura do arquivo");
        }
    }

    public Token nextToken()
    {
        Token tok = new Token(EnumToken.UNDEF);

        String lexema;
        char ch = inputIt.current();
        while (true)
        {
            //Consome espaços em branco e volta para o estado inicial
            if (inputIt.current() == '\n' || inputIt.current() == '\t'
                    || inputIt.current() == '\r' || inputIt.current() == '\f' 
                    || inputIt.current() == ' ')
            {
                while(inputIt.current() == '\n' || inputIt.current() == '\t'
                         || inputIt.current() == '\r' || inputIt.current() == '\f'
                         || inputIt.current() == ' ')
                {
                    if(inputIt.current() == '\n')
                        lineNumber++;

                    inputIt.next();
                }
            }
            else if(inputIt.current() == '/')
            {
                inputIt.next();
                if(inputIt.current() == '/'){
                        while(inputIt.current() != '\n')
                            inputIt.next();
                        lineNumber++;
                        inputIt.next();
                }
                else if(inputIt.current() == '*'){
                        inputIt.next();
                        while(true)
                        {
                            if(inputIt.current() == '\n')
                                lineNumber++;
                            if(inputIt.current() == '*')
                                if(inputIt.next() == '/')
                                    break;
                            inputIt.next();
                        }
                }
                else
                    inputIt.previous();
            }

            //Operadores aritméticos
            else if (inputIt.current() == '+' || inputIt.current() == '-'
                    || inputIt.current() == '*' || inputIt.current() == '/'
                    || inputIt.current() == '%')
            {
                tok.attribute = EnumToken.ARITHOP;

                switch(inputIt.current())
                {
                    case '+':
                        tok.value     = "+";
                        tok.name = EnumToken.PLUS;
                        inputIt.next();
                        break;
                    case '-':
                        tok.value     = "-";
                        tok.name = EnumToken.MINUS;
                        inputIt.next();
                        break;
                    case '*':
                        tok.value     = "*";
                        tok.name = EnumToken.MULT;
                        inputIt.next();
                        break;
                    case '/':
                        tok.value = "/";
                        tok.attribute = EnumToken.DIV;
                        break;
                }
                tok.lineNumber = lineNumber;
                return tok;
            }

            //Operadores relacionais
            else if (inputIt.current() == '<' || inputIt.current() == '>'
                        || inputIt.current() == '!' || inputIt.current() == '=')
            {
                tok.lineNumber = lineNumber;

                if(inputIt.current() == '!' || inputIt.current() == '=')
                {
                    switch(inputIt.current())
                    {
                        case '!':
                            inputIt.next();
                            end = inputIt.getIndex();
                            lexema = new String(input.substring(begin, end));
                            if(inputIt.current() == '=')
                            {
                                tok.value     = lexema;
                                tok.name = EnumToken.NE;
                                tok.attribute = EnumToken.RELOP;
                            }
                            else
                            {
                                tok.value     = lexema;
                                tok.name = EnumToken.NOT;
                            }
                            break;
                        case '=':
                            inputIt.next();
                            end = inputIt.getIndex();
                            lexema = new String(input.substring(begin, end));
                            if(inputIt.current() == '=')
                            {
                                tok.value     = lexema;
                                tok.name = EnumToken.EQ;
                                tok.attribute = EnumToken.RELOP;
                            }
                            else
                            {
                                tok.value     = lexema;
                                tok.name = EnumToken.ATTRIB;
                            }
                            break;
                    }

                    return tok;
                }

                tok.attribute = EnumToken.RELOP;

                switch(inputIt.current())
                {
                    case '<':
                        tok.value     = "<";
                        tok.name = EnumToken.LT;
                        inputIt.next();
                        break;
                    case '>':
                        tok.value     = ">";
                        tok.name = EnumToken.GT;
                        inputIt.next();
                        break;
                }

                return tok;
            }

            //Separadores
            else if (inputIt.current() == '(' || inputIt.current() == ')'
                        || inputIt.current() == '[' || inputIt.current() == ']'
                        || inputIt.current() == '{' || inputIt.current() == '}'
                        || inputIt.current() == ';' || inputIt.current() == '.'
                        || inputIt.current() == ',')
            {
                tok.lineNumber = lineNumber;

                tok.attribute = EnumToken.SEP;

                switch(inputIt.current())
                {
                    case '(':
                        tok.value     = "(";
                        tok.name = EnumToken.LPARENTHESE;
                        inputIt.next();
                        break;
                    case ')':
                        tok.value     = ")";
                        tok.name = EnumToken.RPARENTHESE;
                        inputIt.next();
                        break;
                    case '[':
                        tok.value     = "[";
                        tok.name = EnumToken.LBRACKET;
                        inputIt.next();
                        break;
                    case ']':
                        tok.value     = "]";
                        tok.name = EnumToken.RBRACKET;
                        inputIt.next();
                        break;
                    case '{':
                        tok.value     = "{";
                        tok.name = EnumToken.LBRACE;
                        inputIt.next();
                        break;
                    case '}':
                        tok.value     = "}";
                        tok.name = EnumToken.RBRACE;
                        inputIt.next();
                        break;
                    case ';':
                        tok.value     = ";";
                        tok.name = EnumToken.SEMICOLON;
                        inputIt.next();
                        break;
                    case ',':
                        tok.value     = ",";
                        tok.name = EnumToken.COMMA;
                        inputIt.next();
                        break;
                    case '.':
                        tok.value     = ".";
                        tok.name = EnumToken.PERIOD;
                        inputIt.next();
                        break;
                }
                return tok;
            }

            //operadores logicos
            else if (inputIt.current() == '&')
            {
                inputIt.next();
                if(inputIt.current() == '&')
                {
                    tok.value     = "&&";
                    tok.attribute = EnumToken.LOGOP;
                    tok.name      = EnumToken.AND;
                    tok.lineNumber = lineNumber;
                    inputIt.next();
                    return tok;
                }
                SyntaticError();
            }
            //ID
            else if(Character.isLetter(inputIt.current()))
            {
                begin = inputIt.getIndex();
                end = begin;
                while(Character.isLetterOrDigit(inputIt.current()) || inputIt.current() == '_')
                {
                    inputIt.next();
                }
                end = inputIt.getIndex();
                lexema = new String(input.substring(begin, end));
                if("System".equals(lexema) && inputIt.current() == '.'){
                    while(Character.isLetterOrDigit(inputIt.current()) || inputIt.current() == '_'
                          || inputIt.current() == '.'){
                        inputIt.next();
                    }
                }
                end = inputIt.getIndex();
                lexema = input.substring(begin,end);
    	        //palavras reservadas
                STEntry entry = st.get(lexema);
                if (entry != null)
                    tok.name = entry.token.name;
                else{
                    if(lexema.startsWith("System."))
                        SyntaticError();
                    tok.name = EnumToken.ID;
                }


                tok.value = lexema;
                tok.lineNumber = lineNumber;
                return tok;
            }

            //INTEGER_LITERAL
            else if(Character.isDigit(inputIt.current()))
            {
                while(Character.isDigit(inputIt.current()))
                {
                    inputIt.next();
                }

                end = inputIt.getIndex();
                lexema = new String(input.substring(begin, end));

                tok.name = EnumToken.INTEGER_LITERAL;
                tok.value = lexema;
                tok.lineNumber = lineNumber;

                return tok;
            }

            //EOF
            else if (inputIt.getIndex() == inputIt.getEndIndex())
            {
                tok.lineNumber = lineNumber;
                tok.name = EnumToken.EOF;

                return tok;
            }
            else
                SyntaticError();

        }
    }

    private void SyntaticError() {
        throw new CompilerException("Caractere " + inputIt.current() + " inválido na linha " + lineNumber);
    }
}
