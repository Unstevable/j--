// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a while-statement.
 */
class JWhileStatement extends JStatement {
    public boolean hasBreak; // To see if there is a break statement or not
    public String breakLabel; // The label to go to if there is a break statement
    public boolean hasContinue; // To see if there is a continue statement or not
    public String continueLabel; // The label to go to if there is a continue statement
    // Test expression.
    private JExpression condition;

    // Body.
    private JStatement body;

    /**
     * Constructs an AST node for a while-statement.
     *
     * @param line      line in which the while-statement occurs in the source file.
     * @param condition test expression.
     * @param body      the body.
     */
    public JWhileStatement(int line, JExpression condition, JStatement body) {
        super(line);
        this.condition = condition;
        this.body = body;
    }
    /**
     * {@inheritDoc}
     */
    public JWhileStatement analyze(Context context) {
        // For break statements, push a reference to this while statement
        JMember.enclosingStatement.push(this);

        // Analyze the condition and body of the while statement
        condition = condition.analyze(context);
        condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        body = (JStatement) body.analyze(context);

        // Pop the reference before returning
        JMember.enclosingStatement.pop();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // Create labels for the loop itself and the end of the loop
        String test = output.createLabel();
        String out = output.createLabel();
        // Add the label to loop
        output.addLabel(test);
        // Generate the code for the condition; if it's false, we end the loop
        condition.codegen(output, out, false);
        // If there is a break or continue statement, create the labels
        if (hasBreak){
            breakLabel = output.createLabel();
        }
        if (hasContinue){
            continueLabel = output.createLabel();
        }

        // Generate the code for the body
        body.codegen(output);

        // If the conditional has a continue, add at the appropriate place
        if (hasContinue){
            output.addLabel(continueLabel);
        }
        // Jump back to beginning of the loop
        output.addBranchInstruction(GOTO, test);

        // Add the label for leaving the loop
        output.addLabel(out);

        // If hasBreak is true, create the break label and add it at the end of the body
        if (hasBreak){
            output.addLabel(breakLabel);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JWhileStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("Body", e2);
        body.toJSON(e2);
    }
}
