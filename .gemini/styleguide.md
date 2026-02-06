# RISK REVIEW — JAVA (AI OUTPUT)

### Scope / Relation Evaluate
- **Target Items / Types:** `Is32Promotion`
- **Deep Review Package:** `com.aswatson.*`


Review **ONLY** for the following risk categories:
- Java Runtime Exceptions
- Performance Impact
- Memory Leaks

Ignore style, naming, formatting, and business logic unless they directly cause risks below.
Ignore any risk unless they relate to LIMIT review scope

---


## CORE RULES (MANDATORY)

### Relation / Scope Gate
For each issue you report, include:
- **Relation Evaluate:** `Has | Not relate with Target Items / Types`
- Explain briefly **when/why** it relates (or not).

- Review MUST include method/class/implementation in prompt
- Review MUST include 2-3 level dive into calling method/class/implementation belong to Deep Review Package

---


## 1. Java Runtime Exceptions

For EACH issue found, output:

- **Location:** `<Class>:<Method>:<Line/Block>`
- **Exception Type:** `<NPE | IndexOutOfBounds | ClassCast | IllegalState | etc>`
- **Trigger Condition:** `<exact scenario>`
- **Severity:** `LOW | MEDIUM | HIGH`
- **Fix:** `<concrete code-level suggestion>`
- **Relation Evaluate**: <Has | Not relate with Target Items / Types > <when/why it relate with Target Items / Types>

If none found, state explicitly:
> No concrete runtime exception risks identified.

---

## 2. Performance Impact

For EACH issue found, output:

- **Location:** `<Class>:<Method>`
- **Item/Table:** `Item | Table`
- **Issue Type:** `<DB in loop | O(n²) | blocking | excessive allocation | etc>`
- **Execution Context:** `<request | cronjob | background | OCC | promotion | etc>`
- **Impact Level:** `LOW | MEDIUM | HIGH`
- **Reason:** `<when/why it becomes slow>`
- **Optimization:** `<specific technical fix>`
- **Relation Evaluate**: <Has | Not relate with Target Items / Types > <when/why it relate with Target Items / Types>

If none found, state explicitly:
> No performance risks identified.

---

## 3. Memory Leaks

For EACH issue found, output:

- **Location:** `<Class>:<Field/Method>`
- **Leak Pattern:** `<static reference | unbounded collection | ThreadLocal | listener | cache>`
- **Why It Leaks:** `<GC reasoning>`
- **Runtime Scenario:** `<long-running app | high traffic | cronjob>`
- **Severity:** `LOW | MEDIUM | HIGH`
- **Fix:** `<exact mitigation>`
- **Relation Evaluate**: <Has | Not relate with Target Items / Types > <when/why it relate with Target Items / Types>


If none found, state explicitly:
> No memory leak risks identified.

---

## 4. Summary

| Category | Issues | Highest Severity |
|--------|--------|------------------|
| Runtime Exceptions | X | LOW/MEDIUM/HIGH |
| Performance | X | LOW/MEDIUM/HIGH |
| Memory Leaks | X | LOW/MEDIUM/HIGH |

**PR Verdict:** `APPROVE | APPROVE WITH WARNINGS | BLOCK`  
**Reason:** `<one-line justification>`

---

### Rules for AI
- Be code-specific, no speculation
- Do NOT say “potential issue” without trigger
- Do NOT hallucinate missing code
