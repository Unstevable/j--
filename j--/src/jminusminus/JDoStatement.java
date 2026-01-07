// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a do-statement.
 */
public class JDoStatement extends JStatement {
    public boolean hasBreak; // To see if there is a break statement or not
    public String breakLabel; // The label to go to if there is a break statement
    public boolean hasContinue; // To see if there is a continue statement or not
    public String continueLabel; // The label to go to if there is a continue statement
    // Body.
    private JStatement body;

    // Test expression.
    private JExpression condition;

    /**
     * Constructs an AST node for a do-statement.
     *
     * @param line      line in which the do-statement occurs in the source file.
     * @param body      the body.
     * @param condition test expression.
     */
    public JDoStatement(int line, JStatement body, JExpression condition) {
        super(line);
        this.body = body;
        this.condition = condition;
    }

    /** Added analyze and codegen for project5
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        // For break statements, push a reference to this do statement
        JMember.enclosingStatement.push(this);

        // Analyze the condition and make sure it’s a boolean.
        condition = (JExpression) condition.analyze(context);
        condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        // Analyze the body.
        body = (JStatement) body.analyze(context);

        // Pop the reference to this do statement
        JMember.enclosingStatement.pop();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // Create labels for the beginning and end of the do-while
        String doLabel = output.createLabel();
        String endLabel = output.createLabel();
        // Create a label to go back to if we loop
        output.addLabel(doLabel);

        // If there is a break or continue statement, create the labels
        if (hasBreak){
            breakLabel = output.createLabel();
        }
        if (hasContinue){
            continueLabel = output.createLabel();
        }

        // Generate the body and condition last
        body.codegen(output);

        // If the conditional has a continue, add at the appropriate place
        if (hasContinue){
            output.addLabel(continueLabel);
        }

        // If the condition is false, it jumps to the end label
        condition.codegen(output, endLabel, false);
        // Otherwise, jump to start of loop
        output.addBranchInstruction(GOTO, doLabel);
        output.addLabel(endLabel);

        // If hasBreak is true, add the break label at the end of the body
        if (hasBreak){
            output.addLabel(breakLabel);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JDoStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Body", e1);
        body.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("Condition", e2);
        condition.toJSON(e2);
    }
}
