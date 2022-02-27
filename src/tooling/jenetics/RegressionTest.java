package tooling.jenetics;

import io.jenetics.Mutator;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.ext.SingleNodeCrossover;
import io.jenetics.ext.util.TreeNode;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.*;
import io.jenetics.prog.regression.LossFunction;
import io.jenetics.prog.regression.Regression;
import io.jenetics.prog.regression.Sample;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;

public class RegressionTest {
    static final ISeq<Op<Double>> OPERATIONS = ISeq.of(
            MathOp.ADD,
            MathOp.SUB,
            MathOp.MUL,
            MathOp.MOD
    );

    static final ISeq<Op<Double>> TERMINALS = ISeq.of(
            Var.of("x", 0),
            EphemeralConst.of(() ->
                    (double) RandomRegistry.random().nextInt(10))
    );

    private static final Regression<Double> REGRESSION = Regression.of(
            Regression.codecOf(OPERATIONS, TERMINALS, 5),
            io.jenetics.prog.regression.Error.of(LossFunction::mse),
            Sample.ofDouble(0, 0),
            Sample.ofDouble(1, 1),
            Sample.ofDouble(2, 1),
            Sample.ofDouble(3, 1),
            Sample.ofDouble(4, 1),
            Sample.ofDouble(5, 1)
    );

    public static void main(final String[] args) {
        final Engine<ProgramGene<Double>, Double> engine;
        engine = Engine
                .builder(REGRESSION)
                .minimizing()
                .alterers(
                        new SingleNodeCrossover<>(0.1),
                        new Mutator<>())
                .build();

        final EvolutionResult<ProgramGene<Double>, Double> result = engine
                .stream()
                .limit(Limits.byFitnessThreshold(0.01))
                .peek(RegressionTest::print)
                .collect(EvolutionResult.toBestEvolutionResult());

        print(result);
    }

    private static void print(EvolutionResult<ProgramGene<Double>, Double> result) {
        final TreeNode<Op<Double>> tree = result.bestPhenotype()
                .genotype()
                .gene().toTreeNode();
        MathExpr.rewrite(tree); // Simplify result program.
        System.out.println("Generations: " + result.totalGenerations());
        System.out.println("Function:    " + new MathExpr(tree));
        System.out.println("Error:       " + REGRESSION.error(tree));
    }
}
