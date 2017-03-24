/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mjcompiler;

/**
 *
 * @author Victoria Moraes
 */
public class CompilerException extends RuntimeException
{
    public String msg;

    public CompilerException() 
    {       
        msg = "Unexpected";
    }
    
    public CompilerException(String str)
    {
        super(str);
        msg = "Erro: " + str;
    }
    
    public String toString()
    {
        return msg;
    }
}
