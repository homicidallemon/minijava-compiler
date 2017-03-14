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
    private StringCharacterIterator inputIt;
    private SymbolTable st;
    private int lineNumber;

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

        int begin = 0, end = 0, lineNumber = 0;
        String lexema;
        char ch = inputIt.current();

        while (true)
        {
            //Consome espaços em branco e volta para o estado inicial
            if (inputIt.current() == '\n' || inputIt.current() == '\t'
                    || inputIt.current() == '\r' || inputIt.current() == '\f')
            {
                while(inputIt.current() == '\n' || inputIt.current() == '\t'
                         || inputIt.current() == '\r' || inputIt.current() == '\f')
                {
                    if(inputIt.current() == '\n')
                        lineNumber++;

                    inputIt.next();
                }
            }

            //Operadores aritméticos
            else if (inputIt.current() == '+' || inputIt.current() == '-'
                    || inputIt.current() == '*' || inputIt.current() == '/'
                    || inputIt.current() == '%')
            {
                tok.name = EnumToken.ARITHOP;

                switch(inputIt.current())
                {
                    case '+':
                        tok.value     = '+';
                        tok.attribute = EnumToken.PLUS;
                        inputIt.next();
                        break;
                    case '-':
                        tok.value     = '-';
                        tok.attribute = EnumToken.MINUS;
                        inputIt.next();
                        break;
                    case '*':
                        tok.value     = '*';
                        tok.attribute = EnumToken.MULT;
                        inputIt.next();
                        break;
                    case '/':
                        inputIt.next();

                        if(inputIt.current() == '*' || inputIt.current() == '/')    //Ignora comentários
                        {
                            tok.name = null;
                            switch(inputIt.current())
                            {
                                case '*':
                                    while(true)
                                    {
                                        inputIt.next();
                                        if(inputIt.current() == '*')
                                        {
                                            inputIt.next();
                                            if(inputIt.current() == '/')
                                            {
                                                inputIt.next();
                                                break;
                                            }
                                        }
                                        if(inputIt.current() == '\n')
                                            lineNumber++;
                                    }
                                    break;

                                case '/':
                                    do{
                                        inputIt.next();
                                    }while(inputIt.current() != '\n');

				                    lineNumber++;
                                    break;
                            }
                        }
                        else
                        {
                            tok.value     = '/';
                            tok.attribute = EnumToken.DIV;
                            break;
                        }
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
                                tok.attribute = EnumToken.NE;
                                tok.name = EnumToken.RELOP;
                            }
                            else
                            {
                                tok.value     = lexema;
                                tok.attribute = EnumToken.NOT;
                            }
                            break;
                        case '=':
                            inputIt.next();
                            end = inputIt.getIndex();
                            lexema = new String(input.substring(begin, end));
                            if(inputIt.current() == '=')
                            {
                                tok.value     = lexema;
                                tok.attribute = EnumToken.EQ;
                                tok.name = EnumToken.RELOP;
                            }
                            else
                            {
                                tok.value     = lexema;
                                tok.attribute = EnumToken.ATTRIB;
                            }
                            break;
                    }

                    return tok;
                }

                tok.name = EnumToken.RELOP;

                switch(inputIt.current())
                {
                    case '<':
                        tok.value     = '<';
                        tok.attribute = EnumToken.LT;
                        inputIt.next();
                        break;
                    case '>':
                        tok.value     = '>';
                        tok.attribute = EnumToken.GT;
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

                tok.name = EnumToken.SEP;

                switch(inputIt.current())
                {
                    case '(':
                        tok.value     = '(';
                        tok.attribute = EnumToken.LPARENTHESE;
                        inputIt.next();
                        break;
                    case ')':
                        tok.value     = ')';
                        tok.attribute = EnumToken.RPARENTHESE;
                        inputIt.next();
                        break;
                    case '[':
                        tok.value     = '[';
                        tok.attribute = EnumToken.LBRACKET;
                        inputIt.next();
                        break;
                    case ']':
                        tok.value     = ']';
                        tok.attribute = EnumToken.RBRACKET;
                        inputIt.next();
                        break;
                    case '{':
                        tok.value     = '{';
                        tok.attribute = EnumToken.LBRACE;
                        inputIt.next();
                        break;
                    case '}':
                        tok.value     = '}';
                        tok.attribute = EnumToken.RBRACE;
                        inputIt.next();
                        break;
                    case ';':
                        tok.value     = ';';
                        tok.attribute = EnumToken.SEMICOLON;
                        inputIt.next();
                        break;
                    case ',':
                        tok.value     = ',';
                        tok.attribute = EnumToken.COMMA;
                        inputIt.next();
                        break;
                    case '.':
                        tok.value     = '.';
                        tok.attribute = EnumToken.PERIOD;
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
                    tok.attribute = EnumToken.AND;
                    tok.name      = EnumToken.LOGOP;
                    inputIt.next();
                }
                else SyntaticError();
            }

	
	    begin = inputIt.getIndex();
	    end = begin;
            //ID
            if(Character.isLetter(inputIt.current()))
            {
                while(Character.isLetterOrDigit(inputIt.current()) || inputIt.current() == '_')
                {
                    inputIt.next();
                }
                end = inputIt.getIndex();
                lexema = new String(input.substring(begin, end));
		    
		    //println??

    	        //palavras reservadas
                STEntry entry = st.get(lexema);

                if (entry != null)
                    tok.name = entry.token.name;
                else
                    tok.name = EnumToken.ID;


                tok.value = lexema;
                tok.lineNumber = lineNumber;

                return tok;
            }

            //INTEGER_LITERAL
            if(Character.isDigit(inputIt.current()))
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
            if (inputIt.getIndex() == inputIt.getEndIndex())
            {
                tok.lineNumber = lineNumber;
                tok.name = EnumToken.EOF;

                return tok;
            }

            else SyntacticError();

        }
    }
}
