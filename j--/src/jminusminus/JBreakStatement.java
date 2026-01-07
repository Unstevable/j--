// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * An AST node for a break-statement.
 */
public class JBreakStatement extends JStatement {
    private JStatement enclosingStatement; // For the given control flow's reference
    /**
     * Constructs an AST node for a break-statement.
     *
     * @param line line in which the break-statement occurs in the source file.
     */
    public JBreakStatement(int line) {
        super(line);
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        enclosingStatement = JMember.enclosingStatement.peek();
        // Check each control-flow statement; set the hasBreak value to true
        if (enclosingStatement instanceof JDoStatement){
            ((JDoStatement) enclosingStatement).hasBreak = true;
        } else if (enclosingStatement instanceof JWhileStatement){
            ((JWhileStatement) enclosingStatement).hasBreak = true;
        } else if (enclosingStatement instanceof JForStatement){
            ((JForStatement) enclosingStatement).hasBreak = true;
        } else if (enclosingStatement instanceof JSwitchStatement){
            ((JSwitchStatement) enclosingStatement).hasBreak = true;
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        String controlLabel;
        // Check for each of the control-flow statements, and access the break label values
        // Then, add an unconditional jump to that corresponding label
        if (enclosingStatement instanceof JDoStatement){
            controlLabel = ((JDoStatement) enclosingStatement).breakLabel;
            output.addBranchInstruction(GOTO, controlLabel);
        } else if (enclosingStatement instanceof JWhileStatement){
            controlLabel = ((JWhileStatement) enclosingStatement).breakLabel;
            output.addBranchInstruction(GOTO, controlLabel);
        } else if (enclosingStatement instanceof JForStatement){
            controlLabel = ((JForStatement) enclosingStatement).breakLabel;
            output.addBranchInstruction(GOTO, controlLabel);
        } else if (enclosingStatement instanceof JSwitchStatement){
            controlLabel = ((JSwitchStatement) enclosingStatement).breakLabel;
            output.addBranchInstruction(GOTO, controlLabel);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JBreakStatement:" + line, e);
    }
}
