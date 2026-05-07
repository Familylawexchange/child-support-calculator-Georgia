#!/usr/bin/env python3
"""
Georgia Child Support Calculator (educational estimate)

Implements a simplified worksheet-style estimate aligned to Georgia's
income-shares model (O.C.G.A. § 19-6-15).

Important:
- This is NOT legal advice.
- Georgia guideline worksheets include additional adjustments and case-specific
  factors that are not fully represented here.
"""

from dataclasses import dataclass


# Terminal theme inspired by the Georgia Family Law Resource Center palette.
NAVY = "\033[38;2;20;32;84m"
GOLD = "\033[38;2;243;178;94m"
ORANGE = "\033[38;2;244;114;22m"
MUTED = "\033[38;2;206;213;224m"
WHITE = "\033[97m"
BOLD = "\033[1m"
RESET = "\033[0m"


BASIC_SUPPORT_TABLE_MONTHLY = [
    # (combined_monthly_income_ceiling, basic_support_for_1_child, for_2, for_3, for_4)
    (1500, 285, 430, 500, 560),
    (2000, 345, 520, 605, 680),
    (2500, 405, 605, 705, 790),
    (3000, 460, 690, 805, 900),
    (3500, 510, 765, 895, 1000),
    (4000, 555, 835, 980, 1090),
    (4500, 600, 905, 1060, 1180),
    (5000, 640, 970, 1140, 1270),
    (6000, 715, 1085, 1275, 1425),
    (7000, 785, 1190, 1400, 1560),
    (8000, 850, 1295, 1525, 1700),
    (9000, 910, 1385, 1630, 1820),
    (10000, 970, 1470, 1730, 1930),
]


@dataclass
class ParentInputs:
    gross_monthly_income: float
    preexisting_child_support_paid: float = 0.0
    health_insurance_for_children_paid: float = 0.0
    work_related_childcare_paid: float = 0.0
    extraordinary_expenses_paid: float = 0.0


@dataclass
class CalculationResult:
    combined_adjusted_income: float
    basic_child_support_obligation: float
    total_support_obligation: float
    parent_a_share_percent: float
    parent_b_share_percent: float
    parent_a_presumptive_obligation: float
    parent_b_presumptive_obligation: float


def adjusted_income(parent: ParentInputs) -> float:
    return max(parent.gross_monthly_income - parent.preexisting_child_support_paid, 0.0)


def lookup_basic_support(combined_income: float, number_of_children: int) -> float:
    if number_of_children < 1 or number_of_children > 4:
        raise ValueError("This estimator currently supports 1-4 children.")

    for row in BASIC_SUPPORT_TABLE_MONTHLY:
        ceiling = row[0]
        if combined_income <= ceiling:
            return row[number_of_children]

    # Simple extrapolation above highest band.
    highest = BASIC_SUPPORT_TABLE_MONTHLY[-1]
    highest_ceiling = highest[0]
    highest_amount = highest[number_of_children]
    # Add 6% of income above table ceiling as a conservative continuation.
    return highest_amount + ((combined_income - highest_ceiling) * 0.06)


def calculate_support(parent_a: ParentInputs, parent_b: ParentInputs, number_of_children: int) -> CalculationResult:
    adj_a = adjusted_income(parent_a)
    adj_b = adjusted_income(parent_b)
    combined = adj_a + adj_b

    if combined <= 0:
        raise ValueError("Combined adjusted income must be greater than 0.")

    basic_obligation = lookup_basic_support(combined, number_of_children)

    add_ons = (
        parent_a.health_insurance_for_children_paid
        + parent_b.health_insurance_for_children_paid
        + parent_a.work_related_childcare_paid
        + parent_b.work_related_childcare_paid
        + parent_a.extraordinary_expenses_paid
        + parent_b.extraordinary_expenses_paid
    )

    total_obligation = basic_obligation + add_ons

    share_a = adj_a / combined
    share_b = adj_b / combined

    presumptive_a = total_obligation * share_a
    presumptive_b = total_obligation * share_b

    return CalculationResult(
        combined_adjusted_income=combined,
        basic_child_support_obligation=basic_obligation,
        total_support_obligation=total_obligation,
        parent_a_share_percent=share_a * 100,
        parent_b_share_percent=share_b * 100,
        parent_a_presumptive_obligation=presumptive_a,
        parent_b_presumptive_obligation=presumptive_b,
    )


def _money(value: float) -> str:
    return f"${value:,.2f}"


def _themed(text: str, *styles: str) -> str:
    return f"{''.join(styles)}{text}{RESET}"


def prompt_parent(name: str) -> ParentInputs:
    print(f"\nEnter {name} information:")
    gross = float(input("Gross monthly income: ").strip())
    prior = float(input("Preexisting child support paid monthly (0 if none): ").strip() or "0")
    health = float(input("Monthly health insurance for children paid by this parent: ").strip() or "0")
    childcare = float(input("Monthly work-related childcare paid by this parent: ").strip() or "0")
    extra = float(input("Monthly extraordinary child-rearing expenses paid by this parent: ").strip() or "0")

    return ParentInputs(
        gross_monthly_income=gross,
        preexisting_child_support_paid=prior,
        health_insurance_for_children_paid=health,
        work_related_childcare_paid=childcare,
        extraordinary_expenses_paid=extra,
    )


def main() -> None:
    print(_themed("Georgia Child Support Calculator", BOLD, GOLD))
    print(_themed("Family Law Resource Center Theme", ORANGE))
    print(_themed("Based on Georgia's income-shares framework under O.C.G.A. § 19-6-15.", NAVY))
    print(_themed("This tool is educational and not legal advice.\n", MUTED))

    children = int(input("Number of children in this case (1-4): ").strip())
    parent_a = prompt_parent("Parent A")
    parent_b = prompt_parent("Parent B")

    result = calculate_support(parent_a, parent_b, children)

    print(_themed("\n===== Estimated Georgia Child Support Worksheet Summary =====", BOLD, NAVY))
    print(_themed(f"Combined adjusted monthly income: {_money(result.combined_adjusted_income)}", WHITE))
    print(_themed(f"Basic child support obligation: {_money(result.basic_child_support_obligation)}", WHITE))
    print(_themed(f"Total support obligation (with add-ons): {_money(result.total_support_obligation)}", WHITE))
    print(_themed(f"Parent A income share: {result.parent_a_share_percent:.2f}%", GOLD))
    print(_themed(f"Parent B income share: {result.parent_b_share_percent:.2f}%", GOLD))
    print(_themed(f"Parent A presumptive obligation: {_money(result.parent_a_presumptive_obligation)}", ORANGE))
    print(_themed(f"Parent B presumptive obligation: {_money(result.parent_b_presumptive_obligation)}", ORANGE))

    print(_themed("\nNote: Final court-ordered support may differ due to deviations,", MUTED))
    print(_themed("parenting time adjustments, low/high income rules, and judicial findings.", MUTED))


if __name__ == "__main__":
    main()
