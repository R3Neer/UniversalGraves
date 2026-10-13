# Universal Graves 3.12.0-r3neer.beta.1+26.2

First public beta of the R3Neer fork, based on Universal Graves 3.12.0 for Minecraft 26.2.

## Features

- Dynamic level prices calculated from the grave's remaining contents, equivalent item stacks and enchantments.
- Configurable owner and non-owner price multipliers and enchantment counting modes.
- Optional per-player paid access, supported by both dynamic and static costs and persisted with grave data.
- Protected graves allow eligible players to open the payment menu without granting unpaid item access.
- Removing protection does not bypass the payment requirement.
- Spanish translations and support for existing static cost configurations.

## Installation

Install the regular JAR on Minecraft 26.2 Fabric with Java 25, Fabric API and Polymer. Replace the upstream Universal Graves JAR; do not install both. The sources JAR is for development.

The default configuration retains free access. Configure `interactions.unlocking_cost` for paid access and enable `interactions.allow_non_owner_paid_unlock` for individual payments. Reload changes with `/graves reload`.

## Validation

- 8 unit tests covering dynamic cost arithmetic, stack grouping, limits and cost serialization.
- 5 fork server integration tests covering insufficient funds, exact charging, protected access, multiple players, repeat payment, NBT persistence, remaining contents, configuration reload and legacy global/static access.
- 1 Fabric GameTest registration sanity check.
- Release JARs are taken from the successful CI build of the exact target commit.

This is beta software. Back up worlds and configuration before updating. Full modpack and real multiplayer playtesting remain manual checks.
