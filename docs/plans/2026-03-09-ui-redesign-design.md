# Dockify UI Redesign — Design Document
**Date:** 2026-03-09
**Approach:** Option B — Design System First, Then Screens

---

## Overview

Redesign Dockify's UI with three goals:
1. Adopt a clean minimal light design language (Notion-inspired)
2. Collapse 4 bottom nav tabs to 2 focused tabs
3. Refocus the Health screen on medical data, not gamification

---

## Section 1: Design System

### Color Palette

| Token | Value | Use |
|-------|-------|-----|
| `background` | `#FAFAF9` | Screen backgrounds |
| `surface` | `#FFFFFF` | Cards |
| `surfaceVariant` | `#F5F4F2` | Secondary cards, input fields, avatar backgrounds |
| `primary` | `#2D2D2D` | Primary actions, key text |
| `secondary` | `#6B7280` | Secondary text, icons |
| `accent` | `#4F7FE8` | Active states, progress, links |
| `divider` | `#E8E7E4` | Borders, separators |
| `success` | `#22C55E` | Good/excellent health status |
| `warning` | `#F59E0B` | Warning health status |
| `error` | `#EF4444` | Critical / errors |
| `textPrimary` | `#2D2D2D` | Main body text |
| `textSecondary` | `#6B7280` | Secondary labels |
| `textTertiary` | `#9CA3AF` | Captions, units, timestamps |

Health status colors (excellent → critical) are retained but muted to match the warmer palette.

### Typography

| Style | Size | Weight | Color | Notes |
|-------|------|--------|-------|-------|
| Hero metric value | 52sp | 700 | `#2D2D2D` | Vital card drill-down |
| Metric value | 28sp | 700 | `#2D2D2D` | Summary grid cards |
| Section header | 11sp | 600 | `#9CA3AF` | ALL CAPS, letter-spaced |
| Card title | 16sp | 600 | `#2D2D2D` | — |
| Body | 14sp | 400 | `#4B4B4B` | — |
| Caption / Unit | 12sp | 400 | `#9CA3AF` | — |

### Elevation & Borders
- Cards: `1px` border in `#E8E7E4`, no heavy shadow
- Elevation: `1dp` max with color `#0000000A`
- No gradient card backgrounds (gradients only on charts/sparklines)

### Shape
| Element | Corner Radius |
|---------|--------------|
| Cards | `12dp` |
| Chips / badges | `6dp` |
| Buttons | `8dp` |
| Avatar circles | `50%` (fully round) |

---

## Section 2: Navigation Structure

### Bottom Nav — 2 Tabs Only

| Tab | Icon | Label | Route |
|-----|------|-------|-------|
| 1 | `MonitorHeart` | Health | `HealthRoute` |
| 2 | `PeopleAlt` | Nearby | `NearbyRoute` |

### Route Changes

**Removed:**
- `HomeRoute` — merged into `HealthRoute`
- `HealthDashboardRoute` — replaced by `HealthRoute`

**Kept / Renamed:**
| Route | Access Method |
|-------|---------------|
| `HealthRoute` | Bottom nav tab 1 |
| `NearbyRoute` | Bottom nav tab 2 |
| `HealthDetailRoute(metricType: String)` | Tap vital card from Health tab |
| `ProfileRoute` | Profile avatar icon in Nearby top bar |
| `SettingsRoute` | Settings icon in Health top bar |
| `LoginRoute`, `RegisterRoute`, `ForgotPasswordRoute` | Auth flow |

### Top Bar Patterns
- **Health tab**: "Health" title (left) + settings icon (right)
- **Nearby tab**: "Nearby" title (left) + profile avatar circle with initials (right) → pushes `ProfileRoute`

### Back Stack
- Bottom nav taps call `navigateToRoot(route)` — clears stack to tab root
- Profile and Settings are standard push/pop screens

---

## Section 3: Health Tab

Focus: medical data at a glance with drill-down. No gamification (no streaks, no goals progress, no activity rings as hero).

### Layout (LazyColumn, top to bottom)

**Top Bar**
- Title: "Health" — 20sp, weight 700
- Subtitle inline: "Synced 2 min ago" — 11sp, `#9CA3AF`
- Right icon: settings

**1. Status Overview Card**
- Full-width white card, `1px` border
- Compact grid of all vitals: each cell shows metric name + current value + colored status dot (green / yellow / red)
- Purpose: single glance to know if everything is OK or something needs attention

**2. Section Header: `VITALS`**

**3. Vitals Grid (2-column)**
Each card:
- Icon (colored, 20dp) — top left
- Status dot — top right
- Metric name — 12sp, `#9CA3AF`
- Value — 28sp, 700, `#2D2D2D`
- Unit — 12sp, `#9CA3AF`
- 7-day sparkline trend — bottom of card
- Tap → `HealthDetailRoute` with full chart, min/max/avg, normal range reference

**4. Section Header: `AI INSIGHT`**

**5. AI Recommendation Card**
- White card, left `3dp` accent border in `#4F7FE8`
- Lightbulb icon + recommendation text based on actual vitals
- "Refresh" text button bottom-right in `#4F7FE8`

**6. Section Header: `ACTIVITY`** *(lower priority)*
- Steps, calories, distance shown as simple stat rows — not hero content

**7. Sync Status Footer**
- "Last synced X min ago" — 11sp, `#9CA3AF`, centered
- Sync errors appear as slim inline notice here, not banners

**Removed from current Health screen:**
- Streak badge
- Daily goals progress bar
- Activity rings as hero
- Gamification language

---

## Section 4: Nearby Tab + Profile Screen

### Nearby Tab

**Top Bar**
- Title: "Nearby" (left)
- Right: profile avatar circle (user's initials, `#F5F4F2` bg) → tapping pushes `ProfileRoute`

**Layout (LazyColumn)**

1. **Your Location Card** — white card, green dot + "Your location" label + human-readable area or coordinates
2. **Section Header: `X PEOPLE NEARBY`** — live count
3. **User Cards** — per nearby user:
   - Anonymous avatar (initials or generic icon)
   - Anonymous ID + distance away ("350m away") — no raw lat/lng
   - Chevron right — future: tap to open message sheet
4. **Empty State** — centered icon + "No one nearby right now"

### Profile Screen (pushed screen, not a tab)

**Top Bar**: back arrow + "Profile" title + edit icon (right)

**Layout (LazyColumn)**

1. **Profile Header** — 64dp initials avatar + name + email
2. **Section: `HEALTH INFO`** — blood type, DOB, height, weight as label/value rows with `1px` dividers
3. **Section: `EMERGENCY CONTACTS`** — contact list + "Add contact" row
4. **Section: `CONNECTED SOURCES`** — Health Connect / HealthKit rows with green/gray status dots
5. **Section: `ACCOUNT`** — Settings row, Sign out row

---

## Key Principles

- Medical data is the primary content — not steps, not streaks
- Color communicates health status only (green = good, red = critical) — not branding
- Every screen is scannable in under 3 seconds
- No gradients, glass effects, or decorative backgrounds
- Notion-style section headers (`ALL CAPS`, letter-spaced, muted) replace card titles as organizational elements
