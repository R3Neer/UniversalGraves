# Discovery-first documentation plan

## Plan before editing — 2026-09-09

- Audit the public branch and existing documentation; use an isolated checkout so
  concurrent local implementation is not accidentally committed or documented as released.
- Keep a short README: purpose, one first step, truthful experiments, installation,
  essential caveats and an explicit link to the complete guide.
- Preserve existing detailed information in docs/GUIDE.md, with contents/navigation
  and repaired relative links. Do not silently remove technical guidance.
- Identify this repository as a fork of Patbox's mod; retain upstream links and license. Keep /graves and configurable protection/expiry visible. Move long configuration examples and model instructions to a reference guide. Do not invent custom fork features absent from the public branch.
- Review against these criteria, correct omissions or justify changes, then review
  again. Check relative links and Markdown diff. Documentation-only commit/push;
  no version bump, gameplay mutation, release or binary installation.

## Review

### First review and adjustment

Public branch is the upstream-derived b691be7 baseline, not the locally customized dynamic-cost implementation. Do not advertise those unpublished changes. Retained all upstream command/config/model material and attribution. Narrowed the README's loader claim to the checked Fabric branch; inherited Quilt wording in the guide is explicitly scoped as upstream documentation.

### Repeat review

- The short README offers a concrete first step and optional experiments without
  exposing every rule. Essential requirements and operational caveats remain.
- GUIDE.md preserves the original detailed reference, with contents and a return
  link. All local links and heading anchors in both pages resolve.
- Reviewed attribution, claims and loss of information against the public source.
  No gameplay files, versions, dependencies or local concurrent edits changed.
- Documentation-only validation: link/anchor checks and git diff whitespace checks;
  no new runtime/gameplay validation is claimed by this editorial commit.
