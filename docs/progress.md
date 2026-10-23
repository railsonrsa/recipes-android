# Development progress

How the app grew, one slice at a time. Each slice is an issue of the [implementation map](https://github.com/railsonrsa/recipes-android/issues/17), built as small stacked pull requests. The screenshots and videos are the ones attached to those pull requests. Times are UTC, October 2026.

## Decisions (#1)

21/10 → 22/10 · PRs [#9](https://github.com/railsonrsa/recipes-android/pull/9), [#10](https://github.com/railsonrsa/recipes-android/pull/10), [#11](https://github.com/railsonrsa/recipes-android/pull/11), [#12](https://github.com/railsonrsa/recipes-android/pull/12), [#13](https://github.com/railsonrsa/recipes-android/pull/13), [#14](https://github.com/railsonrsa/recipes-android/pull/14), [#15](https://github.com/railsonrsa/recipes-android/pull/15), [#16](https://github.com/railsonrsa/recipes-android/pull/16)

Before any code, every open question got its own issue on the [decision map](https://github.com/railsonrsa/recipes-android/issues/1) and ended in a doc: fake endpoint research ([#2](https://github.com/railsonrsa/recipes-android/issues/2)), [glossary](../GLOSSARY.md) and [domain](specs/domain.md) ([#3](https://github.com/railsonrsa/recipes-android/issues/3)), endpoint contract, offline flow and [ADR 0001](adr/0001-cache-server-results-per-search.md) ([#4](https://github.com/railsonrsa/recipes-android/issues/4)), [modules](specs/architecture.md) ([#5](https://github.com/railsonrsa/recipes-android/issues/5)), the filter sheet layout picked from a [prototype](https://claude.ai/artifact/GHJzuJmg3UbMPv8wCMv3kw) ([#6](https://github.com/railsonrsa/recipes-android/issues/6)), [quality gates](specs/quality.md) ([#7](https://github.com/railsonrsa/recipes-android/issues/7)) and the agent harness ([#8](https://github.com/railsonrsa/recipes-android/issues/8)).

No screenshots: there was no app yet.

## Skeleton ([#18](https://github.com/railsonrsa/recipes-android/issues/18))

Merged 22/10 20:25 · PRs [#28](https://github.com/railsonrsa/recipes-android/pull/28), [#29](https://github.com/railsonrsa/recipes-android/pull/29)

Seven modules, convention plugins, detekt, lint and CI. Then the DESIGN.md theme (colors, type, shapes, light and dark) on an empty Recipes screen.

| recipes light | recipes dark |
| :---: | :---: |
| <img width="200" alt="recipes light" src="https://github.com/user-attachments/assets/4ac10617-6e11-498d-8421-ba087c701477" /> | <img width="200" alt="recipes dark" src="https://github.com/user-attachments/assets/d8afddd5-fd80-4c00-8bd8-78c29b5963ee" /> |

## Tracer: catalog titles ([#19](https://github.com/railsonrsa/recipes-android/issues/19))

Merged 22/10 21:24 · PRs [#31](https://github.com/railsonrsa/recipes-android/pull/31), [#32](https://github.com/railsonrsa/recipes-android/pull/32), [#33](https://github.com/railsonrsa/recipes-android/pull/33)

The thinnest path through every layer: the fake endpoint answers from the asset, Room saves the catalog, the list shows the saved titles.

No screenshots were attached to [#33](https://github.com/railsonrsa/recipes-android/pull/33).

## Recipe cards ([#20](https://github.com/railsonrsa/recipes-android/issues/20))

Merged 22/10 22:02 · PRs [#35](https://github.com/railsonrsa/recipes-android/pull/35), [#36](https://github.com/railsonrsa/recipes-android/pull/36), [#37](https://github.com/railsonrsa/recipes-android/pull/37)

Times with at most two units, then the card (image with placeholder, diet tags, difficulty bars), then the list of cards with the recipe count.

| light top | light end | dark top |
| :---: | :---: | :---: |
| <img width="200" alt="light top" src="https://github.com/user-attachments/assets/2813dc1e-bbb3-4267-8655-4416dc619d6d" /> | <img width="200" alt="light end" src="https://github.com/user-attachments/assets/d187be5f-1bd9-4474-bd88-14301a582430" /> | <img width="200" alt="dark top" src="https://github.com/user-attachments/assets/824d5bcc-4b6c-4e2c-9c8b-27cb940b713f" /> |
| dark end | | |
| <img width="200" alt="dark end" src="https://github.com/user-attachments/assets/23b8d43e-6925-4c4a-a3de-d9641280746f" /> | | |

## Loading, errors and offline ([#21](https://github.com/railsonrsa/recipes-android/issues/21))

Merged 22/10 22:47 · PRs [#40](https://github.com/railsonrsa/recipes-android/pull/40), [#41](https://github.com/railsonrsa/recipes-android/pull/41), [#42](https://github.com/railsonrsa/recipes-android/pull/42)

Nine fake network modes, the list states, and their screens: loading, offline and refresh-failed snackbars, empty catalogue, Offline, Error. The debug Network menu switches the mode.

| network menu | loading | list offline snackbar |
| :---: | :---: | :---: |
| <img width="200" alt="network menu" src="https://github.com/user-attachments/assets/64abf735-04a6-4a83-937f-c2df3faadd44" /> | <img width="200" alt="loading" src="https://github.com/user-attachments/assets/9eececbf-29ea-4ed8-ade7-c542abfa9c0d" /> | <img width="200" alt="list offline snackbar" src="https://github.com/user-attachments/assets/0865a54f-8f9f-4fbc-a1f2-d5a01990537c" /> |
| list refresh failed snackbar | empty catalogue | offline |
| <img width="200" alt="list refresh failed snackbar" src="https://github.com/user-attachments/assets/d4221bca-29f2-44e6-bee5-740c8bec7e02" /> | <img width="200" alt="empty catalogue" src="https://github.com/user-attachments/assets/31cba6c6-c850-425b-90dd-a09d66dc3676" /> | <img width="200" alt="offline" src="https://github.com/user-attachments/assets/3d300aaf-6e69-4838-9993-2b01d747e087" /> |
| error | | |
| <img width="200" alt="error" src="https://github.com/user-attachments/assets/f8b4a791-791d-47ee-910c-056d06708a83" /> | | |

## Recipe detail ([#22](https://github.com/railsonrsa/recipes-android/issues/22))

Merged 23/10 14:15 · PRs [#45](https://github.com/railsonrsa/recipes-android/pull/45), [#46](https://github.com/railsonrsa/recipes-android/pull/46), [#47](https://github.com/railsonrsa/recipes-android/pull/47), [#48](https://github.com/railsonrsa/recipes-android/pull/48), [#49](https://github.com/railsonrsa/recipes-android/pull/49), [#50](https://github.com/railsonrsa/recipes-android/pull/50)

Open a recipe by id; stats, diet tags and description with Read more; ingredients scaled by the servings stepper; steps with their times and a top bar on scroll.

| detail light | detail no image | detail dark |
| :---: | :---: | :---: |
| <img width="200" alt="detail light" src="https://github.com/user-attachments/assets/8bb36ce1-c44a-4a27-8dce-02b2dfa5321d" /> | <img width="200" alt="detail no image" src="https://github.com/user-attachments/assets/816c2dfb-8b8e-4554-8f1c-65dde3e7c8a3" /> | <img width="200" alt="detail dark" src="https://github.com/user-attachments/assets/10132427-11eb-4758-a02f-d0e185f587f7" /> |
| margherita light | margherita dark | risotto no rating |
| <img width="200" alt="margherita light" src="https://github.com/user-attachments/assets/e674a75c-c9a9-48c2-9d19-87e99764108c" /> | <img width="200" alt="margherita dark" src="https://github.com/user-attachments/assets/0d60dcbb-75c5-48b0-812f-29f89d321976" /> | <img width="200" alt="risotto no rating" src="https://github.com/user-attachments/assets/98417706-4a2e-4a7a-b21b-d84b2354c177" /> |
| mousse no cook time | lamb read more | lamb expanded |
| <img width="200" alt="mousse no cook time" src="https://github.com/user-attachments/assets/798ef86f-7061-4cc2-bba3-cb2d171753bd" /> | <img width="200" alt="lamb read more" src="https://github.com/user-attachments/assets/f758d65e-c04d-4e92-ab8b-40d206b99976" /> | <img width="200" alt="lamb expanded" src="https://github.com/user-attachments/assets/69c36b4d-ce66-4891-b7ea-b4ea8bdc2771" /> |
| margherita as written | margherita scaled to 6 | min 1 serving |
| <img width="200" alt="margherita as written" src="https://github.com/user-attachments/assets/2e141039-2009-40ff-88da-aeb240577338" /> | <img width="200" alt="margherita scaled to 6" src="https://github.com/user-attachments/assets/6379354c-51c7-4047-9d4a-8aad2515282d" /> | <img width="200" alt="min 1 serving" src="https://github.com/user-attachments/assets/b40a6624-2d86-46dd-89ea-fa09732c8af1" /> |
| max 20 servings | scaled dark | salmon as written |
| <img width="200" alt="max 20 servings" src="https://github.com/user-attachments/assets/49c03673-c374-462b-a32b-2557d168b2d6" /> | <img width="200" alt="scaled dark" src="https://github.com/user-attachments/assets/77997943-e76a-4760-8bf4-28b88f28b5aa" /> | <img width="200" alt="salmon as written" src="https://github.com/user-attachments/assets/f2cc01fb-edbc-41a2-b110-3404c502e68e" /> |
| salmon scaled to 3 | instructions light | instructions dark |
| <img width="200" alt="salmon scaled to 3" src="https://github.com/user-attachments/assets/fede1671-224b-42f2-a1be-fa070414fc15" /> | <img width="200" alt="instructions light" src="https://github.com/user-attachments/assets/2ba20753-1c79-4010-955d-5e08e839516e" /> | <img width="200" alt="instructions dark" src="https://github.com/user-attachments/assets/80f480a2-c78c-426c-b0f2-245487f251a1" /> |
| tacos no step times | lamb long title top bar | |
| <img width="200" alt="tacos no step times" src="https://github.com/user-attachments/assets/033ecdc6-be2c-49ae-afa4-d051f47446fc" /> | <img width="200" alt="lamb long title top bar" src="https://github.com/user-attachments/assets/d733b23f-f764-46ad-a66e-9fa75707fa93" /> | |

https://github.com/user-attachments/assets/0d835768-d7ba-40ba-a3a9-9b7db41e6de8

https://github.com/user-attachments/assets/cbb7b723-9ae5-495b-a052-edf4c6a21946

https://github.com/user-attachments/assets/740c7eb9-e5e3-4bd4-8bbd-50249ff03025

https://github.com/user-attachments/assets/a8811dd3-02b5-4e36-b858-d9a5f9902b35

## Search by text ([#23](https://github.com/railsonrsa/recipes-android/issues/23))

Merged 23/10 15:03 · PRs [#52](https://github.com/railsonrsa/recipes-android/pull/52), [#53](https://github.com/railsonrsa/recipes-android/pull/53)

Search by title or description, accent and case insensitive, each search saved on its own. The search bar and the No results state.

| light pao | light no results | light offline saved search |
| :---: | :---: | :---: |
| <img width="200" alt="light pao" src="https://github.com/user-attachments/assets/215d2a8d-55f2-410b-aa8e-da61a6ccc1f9" /> | <img width="200" alt="light no results" src="https://github.com/user-attachments/assets/36365775-b60e-44f0-a27d-00fab8482aa4" /> | <img width="200" alt="light offline saved search" src="https://github.com/user-attachments/assets/ec40284f-fe15-4456-b4d3-767f998e2ec6" /> |
| light offline search never made | dark pao | dark no results |
| <img width="200" alt="light offline search never made" src="https://github.com/user-attachments/assets/2e3f5013-5b98-4b6c-bb5c-6406311fb8d5" /> | <img width="200" alt="dark pao" src="https://github.com/user-attachments/assets/e60697d2-5f72-43bd-9f52-e99301a14807" /> | <img width="200" alt="dark no results" src="https://github.com/user-attachments/assets/1e01c312-5d64-4336-bb32-192ca4c3eabe" /> |

## Filters ([#24](https://github.com/railsonrsa/recipes-android/issues/24))

Merged 23/10 17:00 · PRs [#55](https://github.com/railsonrsa/recipes-android/pull/55), [#56](https://github.com/railsonrsa/recipes-android/pull/56), [#57](https://github.com/railsonrsa/recipes-android/pull/57)

Diet, servings and difficulty in the endpoint and the cache key. The filter sheet with a live "Show N recipes", the badge, the active filter chips and Clear all filters.

| light filter button | light sheet show 10 | light vegan halal show 0 |
| :---: | :---: | :---: |
| <img width="200" alt="light filter button" src="https://github.com/user-attachments/assets/9181ef89-251b-4fb8-b2db-3d0308de89fc" /> | <img width="200" alt="light sheet show 10" src="https://github.com/user-attachments/assets/5e59febf-c40b-4f06-a5ab-0b1c742df044" /> | <img width="200" alt="light vegan halal show 0" src="https://github.com/user-attachments/assets/ab20d959-f9c1-40ee-ac83-0492393dc7ff" /> |
| light no results badge chips | light chip removed vegan | dark sheet vegetarian 3 4 easy |
| <img width="200" alt="light no results badge chips" src="https://github.com/user-attachments/assets/25967804-cad5-4eec-a421-fa849f298dc5" /> | <img width="200" alt="light chip removed vegan" src="https://github.com/user-attachments/assets/37636c6a-f1b8-4259-ac6f-c154f46bb472" /> | <img width="200" alt="dark sheet vegetarian 3 4 easy" src="https://github.com/user-attachments/assets/1e3fae2c-4fef-4d40-b62b-6318e29f51a0" /> |
| dark list three chips | dark no network show recipes | |
| <img width="200" alt="dark list three chips" src="https://github.com/user-attachments/assets/7f4dba31-a028-4a67-8cb6-4eb44c3ecb9f" /> | <img width="200" alt="dark no network show recipes" src="https://github.com/user-attachments/assets/7067a206-ead6-4410-bf6c-68c1957e2126" /> | |

## Ingredients and instructions ([#25](https://github.com/railsonrsa/recipes-android/issues/25))

Merged 23/10 18:23 · PRs [#59](https://github.com/railsonrsa/recipes-android/pull/59), [#60](https://github.com/railsonrsa/recipes-android/pull/60), [#61](https://github.com/railsonrsa/recipes-android/pull/61)

Ingredients a recipe must have or must not have, with one Must have / Must not have switch, and a search in the instructions.

| sheet ingredients light | moved snackbar light | list filter chips light |
| :---: | :---: | :---: |
| <img width="200" alt="sheet ingredients light" src="https://github.com/user-attachments/assets/9d67eae1-189e-4dd7-b3f3-74fa87bc53ce" /> | <img width="200" alt="moved snackbar light" src="https://github.com/user-attachments/assets/a9e7e600-7e45-4a92-94d4-1758d8bcea3f" /> | <img width="200" alt="list filter chips light" src="https://github.com/user-attachments/assets/b0bf64e5-f982-4da8-9f61-7f2ac0f69c47" /> |
| sheet ingredients dark | moved snackbar dark | list filter chips dark |
| <img width="200" alt="sheet ingredients dark" src="https://github.com/user-attachments/assets/e0c6aec2-8278-4c38-a63a-53efca1ab3d2" /> | <img width="200" alt="moved snackbar dark" src="https://github.com/user-attachments/assets/9626f015-95af-43f8-8891-176a38bc14bd" /> | <img width="200" alt="list filter chips dark" src="https://github.com/user-attachments/assets/9e38dd78-4927-47c4-a20f-6ff2b4ca2899" /> |

https://github.com/user-attachments/assets/1f2a2e6f-f142-4400-95f3-1a3e0d60bcf1

## Load more ([#26](https://github.com/railsonrsa/recipes-android/issues/26))

Merged 23/10 19:13 · PRs [#63](https://github.com/railsonrsa/recipes-android/pull/63), [#64](https://github.com/railsonrsa/recipes-android/pull/64)

The next page of a search goes into the cache; the list end shows Loading more, a retry when it fails, and All N recipes loaded.

| all loaded light | all loaded dark | loading more light |
| :---: | :---: | :---: |
| <img width="200" alt="all loaded light" src="https://github.com/user-attachments/assets/4f0e7c6d-7ae7-4d06-96b2-d6bc05cce9b9" /> | <img width="200" alt="all loaded dark" src="https://github.com/user-attachments/assets/6500969a-4380-4daf-bfc0-05ebc3da6f73" /> | <img width="200" alt="loading more light" src="https://github.com/user-attachments/assets/3b479f7d-1954-4ab9-9bd5-5df5653d4e31" /> |
| load more failed light | retry loading more light | all loaded after 3 pages light |
| <img width="200" alt="load more failed light" src="https://github.com/user-attachments/assets/1ddc600a-a895-4985-a216-62f06a895950" /> | <img width="200" alt="retry loading more light" src="https://github.com/user-attachments/assets/f8048736-dfb5-4bda-b97d-bbc167cc9f93" /> | <img width="200" alt="all loaded after 3 pages light" src="https://github.com/user-attachments/assets/14863acf-4a46-46b7-97ba-3f20af4a7853" /> |
