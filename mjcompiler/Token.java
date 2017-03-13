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
public class Token
{
    public EnumToken name;
    public EnumToken attribute;
    public String value;
    public int lineNumber;
    //public STEntry tsPtr;

    public Token(EnumToken name)
    {
        this.name = name;
        attribute = EnumToken.UNDEF;
        lineNumber = -1;
        //tsPtr = null;
    }

    public Token(EnumToken name, EnumToken attr)
    {
        this.name = name;
        attribute = attr;
        //tsPtr = null;
    }
}
