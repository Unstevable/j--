// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a conditional expression.
 */
class JConditionalExpression extends JExpression {
    // Test expression.
    private JExpression condition;

    // Then part.
    private JExpression thenPart;

    // Else part.
    private JExpression elsePart;

    /**
     * Constructs an AST node for a conditional expression.
     *
     * @param line      line in which the conditional expression occurs in the source file.
     * @param condition test expression.
     * @param thenPart  then part.
     * @param elsePart  else part.
     */
    public JConditionalExpression(int line, JExpression condition, JExpression thenPart,
                                  JExpression elsePart) {
        super(line);
        this.condition = condition;
        this.thenPart = thenPart;
        this.elsePart = elsePart;
    }

    /** Added analyze and codegen for project5
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // Analyze the condition and make sure it has a boolean value
        condition = (JExpression) condition.analyze(context);
        condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        type = Type.BOOLEAN;
        // Analyze then and else part and make sure they have the same type
        thenPart = (JExpression) thenPart.analyze(context);
        elsePart = (JExpression) elsePart.analyze(context);
        elsePart.type().mustMatchExpected(line(), thenPart.type());
        // Set the type of the expression to that of the consequence
        type = thenPart.type();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // Create labels for the end of the conditional and the else part
        String elseLabel = output.createLabel();
        String endLabel = output.createLabel();

        // If the condition is false, go to the else part, otherwise fall through to the then part
        condition.codegen(output, elseLabel, false);
        thenPart.codegen(output);
        // If there is an else part, jump to after it (the end label)
        if (elsePart != null){
            output.addBranchInstruction(GOTO, endLabel);
        }

        // Code gen for the else part
        output.addLabel(elseLabel);
        if (elsePart != null){
            elsePart.codegen(output);
            output.addLabel(endLabel);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JConditionalExpression:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("ThenPart", e2);
        thenPart.toJSON(e2);
        JSONElement e3 = new JSONElement();
        e.addChild("ElsePart", e3);
        elsePart.toJSON(e3);
    }
}
