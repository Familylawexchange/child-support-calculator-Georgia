# Georgia Child Support Calculator (Educational Estimate)

This repository provides a command-line child support estimator using Georgia's
income-shares model concepts under **O.C.G.A. § 19-6-15**.

## What this calculator does

- Accepts both parents' gross monthly income.
- Applies a simplified adjustment for preexisting child support paid.
- Uses a built-in monthly basic support lookup table for 1-4 children.
- Adds child-specific expenses:
  - Health insurance
  - Work-related childcare
  - Extraordinary child-rearing expenses
- Allocates total obligation by each parent's proportional income share.

## Important legal note

This calculator is **not legal advice** and is **not an official Georgia court worksheet**.
Georgia support determinations can include deviations and factors not fully represented here.
Always verify with the latest official Georgia forms, statutes, and (if needed) a licensed attorney.

## Run

```bash
python3 georgia_child_support_calculator.py
```
