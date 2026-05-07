# Georgia Child Support Calculator (Educational Estimate)

This repository provides a command-line child support estimator in **Java**
using Georgia income-shares model concepts under **O.C.G.A. § 19-6-15**.

## What this calculator does

- Accepts both parents' gross monthly income.
- Applies a simplified adjustment for preexisting child support paid.
- Uses a built-in monthly basic support lookup table for 1-4 children.
- Adds child-specific expenses:
  - Health insurance
  - Work-related childcare
  - Extraordinary child-rearing expenses
- Allocates total obligation by each parent's proportional income share.
- Includes an educational **parenting-time deviation** estimate based on annual
  overnights for each parent.

## Parenting-time deviation model (educational approximation)

This calculator uses a simple, non-official approximation:

- Fewer than 92 overnights: no parenting-time credit.
- 92 to 182 overnights: scaled credit up to 20% of total obligation.
- More than 182 overnights: credit capped at 20% of total obligation.

This is included as an educational demonstration only and is **not** an
official Georgia worksheet formula.

## Important legal note

This calculator is **not legal advice** and is **not an official Georgia court worksheet**.
Georgia support determinations can include deviations and factors not fully represented here.
Always verify with the latest official Georgia forms, statutes, and (if needed) a licensed attorney.

## Compile and run

```bash
javac GeorgiaChildSupportCalculator.java
java GeorgiaChildSupportCalculator
```
