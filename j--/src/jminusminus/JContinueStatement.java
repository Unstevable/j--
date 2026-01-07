// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.GOTO;

/**
 * An AST node for a continue-statement.
 */
public class JContinueStatement extends JStatement {
    private JStatement enclosingStatement; // For the given control flow's reference
    /**
     * Constructs an AST node for a continue-statement.
     *
     * @param line line in which the continue-statement occurs in the source file.
     */
    public JContinueStatement(int line) {
        super(line);
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        enclosingStatement = JMember.enclosingStatement.peek();
        // Check to make sure we're dealing with the specific control-flow; set the continue to true
        if (enclosingStatement instanceof JWhileStatement){
            ((JWhileStatement) enclosingStatement).hasContinue = true;
        } else if (enclosingStatement instanceof JDoStatement){
            ((JDoStatement) enclosingStatement).hasContinue = true;
        } else if (enclosingStatement instanceof JForStatement){
            ((JForStatement) enclosingStatement).hasContinue = true;
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        String controlLabel;
        // Check each of the control flows; access the continue label and generate an unconditional jump
        if (enclosingStatement instanceof JWhileStatement){
            controlLabel = ((JWhileStatement) enclosingStatement).continueLabel;
            output.addBranchInstruction(GOTO, controlLabel);
        } else if (enclosingStatement instanceof JDoStatement){
            controlLabel = ((JDoStatement) enclosingStatement).continueLabel;
            output.addBranchInstruction(GOTO, controlLabel);
        } else if (enclosingStatement instanceof JForStatement){
            controlLabel = ((JForStatement) enclosingStatement).continueLabel;
            output.addBranchInstruction(GOTO, controlLabel);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JContinueStatement:" + line, e);
    }
}
