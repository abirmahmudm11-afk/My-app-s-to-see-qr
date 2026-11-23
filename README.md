# SCAN2MONEY (Web)

Barcode-powered product discovery, store offer comparison, receipt verification, and audited sandbox rewards wallet built with React, TypeScript, Vite, Tailwind CSS, and Firebase.

## Core Features

- **13-Format Barcode Scanner & Validator**: Live browser WebRTC + ZXing camera decoding, uploaded photo decoding, manual GTIN entry, and GS1 Modulo-10 check-digit verification (`EAN-13`, `EAN-8`, `UPC-A`, `UPC-E`, `Code 128`, `Code 39`, `Code 93`, `QR Code`, `Data Matrix`, `PDF417`, `Aztec`, `ITF`, `Codabar`).
- **Attributed Product Catalog**: Lookup priority across verified merchant records in Firestore, Open Food Facts (`world.openfoodfacts.org`) live REST API, local offline cache, and unverified community submissions.
- **Country-Aware Price Comparison**: Filter and report store offers across `US`, `GB`, `DE`, `JP`, `SG`, and `CA` markets with explicit verified vs. unverified provenance labels.
- **Receipt Verification & Rewards Ledger**: Submit retail receipts with client-side SHA-256 duplicate hashing, track pending/approved/rejected statuses, inspect immutable ledger transactions, and redeem non-cash sandbox points (`PTS`).
- **RBAC Admin Operations Console**: Role-gated receipt approval/rejection, atomic wallet crediting, campaign creation and lifecycle management, community product verification, ledger reversals, and immutable audit logs.
- **Accessibility & Localization**: Light/dark themes, reduced-motion toggle, guest explorer mode, marketing opt-in consent, and multi-market currency formatting.

## Development

```bash
npm install
npm run dev
```

## Build & Lint

```bash
npm run lint
npm run build
```
