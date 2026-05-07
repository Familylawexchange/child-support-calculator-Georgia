import java.text.NumberFormat;
import java.util.Locale;
import java.util.Scanner;

/**
 * Georgia Child Support Calculator (educational estimate).
 *
 * Simplified worksheet-style estimate aligned to Georgia's income-shares model
 * concepts under O.C.G.A. § 19-6-15.
 *
 * This is not legal advice.
 */
public class GeorgiaChildSupportCalculator {
    private static final String NAVY = "\u001B[38;2;20;32;84m";
    private static final String GOLD = "\u001B[38;2;243;178;94m";
    private static final String ORANGE = "\u001B[38;2;244;114;22m";
    private static final String MUTED = "\u001B[38;2;206;213;224m";
    private static final String WHITE = "\u001B[97m";
    private static final String BOLD = "\u001B[1m";
    private static final String RESET = "\u001B[0m";

    private static final double[][] BASIC_SUPPORT_TABLE_MONTHLY = {
        // {combined_income_ceiling, support_1_child, support_2, support_3, support_4}
        {1500, 285, 430, 500, 560},
        {2000, 345, 520, 605, 680},
        {2500, 405, 605, 705, 790},
        {3000, 460, 690, 805, 900},
        {3500, 510, 765, 895, 1000},
        {4000, 555, 835, 980, 1090},
        {4500, 600, 905, 1060, 1180},
        {5000, 640, 970, 1140, 1270},
        {6000, 715, 1085, 1275, 1425},
        {7000, 785, 1190, 1400, 1560},
        {8000, 850, 1295, 1525, 1700},
        {9000, 910, 1385, 1630, 1820},
        {10000, 970, 1470, 1730, 1930}
    };

    static class ParentInputs {
        final double grossMonthlyIncome;
        final double preexistingChildSupportPaid;
        final double healthInsuranceForChildrenPaid;
        final double workRelatedChildcarePaid;
        final double extraordinaryExpensesPaid;
        final int annualOvernights;

        ParentInputs(
            double grossMonthlyIncome,
            double preexistingChildSupportPaid,
            double healthInsuranceForChildrenPaid,
            double workRelatedChildcarePaid,
            double extraordinaryExpensesPaid,
            int annualOvernights
        ) {
            this.grossMonthlyIncome = grossMonthlyIncome;
            this.preexistingChildSupportPaid = preexistingChildSupportPaid;
            this.healthInsuranceForChildrenPaid = healthInsuranceForChildrenPaid;
            this.workRelatedChildcarePaid = workRelatedChildcarePaid;
            this.extraordinaryExpensesPaid = extraordinaryExpensesPaid;
            this.annualOvernights = annualOvernights;
        }
    }

    static class CalculationResult {
        final double combinedAdjustedIncome;
        final double basicChildSupportObligation;
        final double totalSupportObligation;
        final double parentASharePercent;
        final double parentBSharePercent;
        final double parentAPresumptiveObligation;
        final double parentBPresumptiveObligation;
        final double parentAParentingTimeDeviation;
        final double parentBParentingTimeDeviation;
        final double parentAFinalObligation;
        final double parentBFinalObligation;

        CalculationResult(
            double combinedAdjustedIncome,
            double basicChildSupportObligation,
            double totalSupportObligation,
            double parentASharePercent,
            double parentBSharePercent,
            double parentAPresumptiveObligation,
            double parentBPresumptiveObligation,
            double parentAParentingTimeDeviation,
            double parentBParentingTimeDeviation,
            double parentAFinalObligation,
            double parentBFinalObligation
        ) {
            this.combinedAdjustedIncome = combinedAdjustedIncome;
            this.basicChildSupportObligation = basicChildSupportObligation;
            this.totalSupportObligation = totalSupportObligation;
            this.parentASharePercent = parentASharePercent;
            this.parentBSharePercent = parentBSharePercent;
            this.parentAPresumptiveObligation = parentAPresumptiveObligation;
            this.parentBPresumptiveObligation = parentBPresumptiveObligation;
            this.parentAParentingTimeDeviation = parentAParentingTimeDeviation;
            this.parentBParentingTimeDeviation = parentBParentingTimeDeviation;
            this.parentAFinalObligation = parentAFinalObligation;
            this.parentBFinalObligation = parentBFinalObligation;
        }
    }

    private static double adjustedIncome(ParentInputs parent) {
        return Math.max(parent.grossMonthlyIncome - parent.preexistingChildSupportPaid, 0.0);
    }

    private static double lookupBasicSupport(double combinedIncome, int numberOfChildren) {
        if (numberOfChildren < 1 || numberOfChildren > 4) {
            throw new IllegalArgumentException("This estimator currently supports 1-4 children.");
        }

        for (double[] row : BASIC_SUPPORT_TABLE_MONTHLY) {
            if (combinedIncome <= row[0]) {
                return row[numberOfChildren];
            }
        }

        double[] highest = BASIC_SUPPORT_TABLE_MONTHLY[BASIC_SUPPORT_TABLE_MONTHLY.length - 1];
        double highestCeiling = highest[0];
        double highestAmount = highest[numberOfChildren];
        return highestAmount + ((combinedIncome - highestCeiling) * 0.06);
    }

    private static double parentingTimeDeviationAmount(double totalObligation, int overnights) {
        // Educational approximation:
        // - below 92 overnights: no deviation
        // - 92-182 overnights: up to 20% credit scaled within that range
        // - above 182 overnights: cap at 20%
        if (overnights < 92) {
            return 0.0;
        }

        double maxCreditRate = 0.20;
        if (overnights >= 182) {
            return totalObligation * maxCreditRate;
        }

        double scaled = (overnights - 92) / 90.0;
        return totalObligation * maxCreditRate * scaled;
    }

    private static CalculationResult calculateSupport(ParentInputs parentA, ParentInputs parentB, int numberOfChildren) {
        double adjustedA = adjustedIncome(parentA);
        double adjustedB = adjustedIncome(parentB);
        double combined = adjustedA + adjustedB;

        if (combined <= 0) {
            throw new IllegalArgumentException("Combined adjusted income must be greater than 0.");
        }

        int combinedOvernights = parentA.annualOvernights + parentB.annualOvernights;
        if (combinedOvernights > 365) {
            throw new IllegalArgumentException("Parenting overnights for both parents cannot exceed 365 total.");
        }

        double basicObligation = lookupBasicSupport(combined, numberOfChildren);

        double addOns =
            parentA.healthInsuranceForChildrenPaid + parentB.healthInsuranceForChildrenPaid
            + parentA.workRelatedChildcarePaid + parentB.workRelatedChildcarePaid
            + parentA.extraordinaryExpensesPaid + parentB.extraordinaryExpensesPaid;

        double totalObligation = basicObligation + addOns;

        double shareA = adjustedA / combined;
        double shareB = adjustedB / combined;

        double presumptiveA = totalObligation * shareA;
        double presumptiveB = totalObligation * shareB;

        double deviationA = parentingTimeDeviationAmount(totalObligation, parentA.annualOvernights);
        double deviationB = parentingTimeDeviationAmount(totalObligation, parentB.annualOvernights);

        double finalA = Math.max(presumptiveA - deviationA, 0.0);
        double finalB = Math.max(presumptiveB - deviationB, 0.0);

        return new CalculationResult(
            combined,
            basicObligation,
            totalObligation,
            shareA * 100,
            shareB * 100,
            presumptiveA,
            presumptiveB,
            deviationA,
            deviationB,
            finalA,
            finalB
        );
    }

    private static String money(double value) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(value);
    }

    private static String themed(String text, String... styles) {
        StringBuilder out = new StringBuilder();
        for (String style : styles) {
            out.append(style);
        }
        out.append(text).append(RESET);
        return out.toString();
    }

    private static ParentInputs promptParent(Scanner scanner, String name) {
        System.out.println("\nEnter " + name + " information:");
        double gross = promptDouble(scanner, "Gross monthly income: ");
        double prior = promptDouble(scanner, "Preexisting child support paid monthly (0 if none): ");
        double health = promptDouble(scanner, "Monthly health insurance for children paid by this parent: ");
        double childcare = promptDouble(scanner, "Monthly work-related childcare paid by this parent: ");
        double extra = promptDouble(scanner, "Monthly extraordinary child-rearing expenses paid by this parent: ");
        int overnights = promptInt(scanner, "Annual overnights with children for this parent (0-365): ");

        if (overnights < 0 || overnights > 365) {
            throw new IllegalArgumentException("Overnights must be between 0 and 365.");
        }

        return new ParentInputs(gross, prior, health, childcare, extra, overnights);
    }

    private static double promptDouble(Scanner scanner, String prompt) {
        System.out.print(prompt);
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(line);
    }

    private static int promptInt(Scanner scanner, String prompt) {
        System.out.print(prompt);
        String line = scanner.nextLine().trim();
        return Integer.parseInt(line);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println(themed("Georgia Child Support Calculator", BOLD, GOLD));
        System.out.println(themed("Family Law Resource Center Theme", ORANGE));
        System.out.println(themed("Based on Georgia's income-shares framework under O.C.G.A. § 19-6-15.", NAVY));
        System.out.println(themed("This tool is educational and not legal advice.\n", MUTED));

        int children = promptInt(scanner, "Number of children in this case (1-4): ");
        ParentInputs parentA = promptParent(scanner, "Parent A");
        ParentInputs parentB = promptParent(scanner, "Parent B");

        CalculationResult result = calculateSupport(parentA, parentB, children);

        System.out.println(themed("\n===== Estimated Georgia Child Support Worksheet Summary =====", BOLD, NAVY));
        System.out.println(themed("Combined adjusted monthly income: " + money(result.combinedAdjustedIncome), WHITE));
        System.out.println(themed("Basic child support obligation: " + money(result.basicChildSupportObligation), WHITE));
        System.out.println(themed("Total support obligation (with add-ons): " + money(result.totalSupportObligation), WHITE));
        System.out.println(themed(String.format("Parent A income share: %.2f%%", result.parentASharePercent), GOLD));
        System.out.println(themed(String.format("Parent B income share: %.2f%%", result.parentBSharePercent), GOLD));
        System.out.println(themed("Parent A presumptive obligation: " + money(result.parentAPresumptiveObligation), ORANGE));
        System.out.println(themed("Parent B presumptive obligation: " + money(result.parentBPresumptiveObligation), ORANGE));
        System.out.println(themed("Parent A parenting-time deviation (credit): -" + money(result.parentAParentingTimeDeviation), WHITE));
        System.out.println(themed("Parent B parenting-time deviation (credit): -" + money(result.parentBParentingTimeDeviation), WHITE));
        System.out.println(themed("Parent A estimated final obligation: " + money(result.parentAFinalObligation), BOLD, ORANGE));
        System.out.println(themed("Parent B estimated final obligation: " + money(result.parentBFinalObligation), BOLD, ORANGE));

        System.out.println(themed("\nNote: Parenting-time deviation here is an educational approximation.", MUTED));
        System.out.println(themed("Final court-ordered support can differ based on statutory findings and judicial discretion.", MUTED));
    }
}
