# PHASE 3: Field Mapping

## Completed in this phase:

### 1. Domain Models
- `FieldMapping` — Individual field mapping with validation rules
- `ValidationRule` — Email, Phone, Date, Number, Length, Regex validation
- `MappingProfile` — Reusable mapping configuration
- `SuccessIndicator` — How to detect successful form submission

### 2. Database Schema
- `FieldMappingEntity` — Store individual mappings
- `MappingProfileEntity` — Store mapping profiles with success indicators

### 3. Repository Pattern
- `MappingRepository` — Full CRUD for mapping profiles
- JSON serialization for complex objects (validation rules, success indicators)
- Support for multiple profiles per dataset

### 4. UI Screens
- `FieldMappingScreen` — Create/edit mappings
  - Profile name and description
  - Add/remove individual field mappings
  - Configure success indicator
  - Save profile

- `MappingProfilesScreen` — Browse and manage profiles
  - List all profiles
  - Create, edit, delete profiles
  - Select profile for automation

### 5. Validation Engine
- `FieldValidator` — Comprehensive validation
  - Required field validation
  - Email format validation
  - Phone format validation
  - Date format validation
  - Number validation
  - Custom regex validation
  - Length constraints

## Mapping Workflow

```
Spreadsheet Columns        Form Fields
────────────────────────   ────────────────────
Name                  →    Name (TEXT, required)
Phone                 →    Mobile (PHONE, required)
Email                 →    Email (EMAIL, required)
DOB                   →    Date of Birth (DATE, required)
Address               →    Address (TEXTAREA, optional)
```

## Success Indicators

After save, the app detects success by:
- `PAGE_CHANGE` — URL changed
- `URL_CHANGE` — Specific URL pattern matched
- `SUCCESS_MESSAGE` — Success message element appeared
- `ELEMENT_APPEARS` — Expected element appeared on page
- `CUSTOM` — Custom indicator (future extension)

## Testing Checklist

- [ ] Create mapping profile with 5+ fields
- [ ] Email validation works correctly
- [ ] Phone validation works correctly
- [ ] Date validation works correctly
- [ ] Number validation works correctly
- [ ] Required field validation works
- [ ] Save profile to database
- [ ] Retrieve saved profile
- [ ] List all profiles
- [ ] Update profile
- [ ] Delete profile
- [ ] UI renders correctly
- [ ] Add/remove mappings dynamically
- [ ] Reuse profile for multiple datasets

## Next: PHASE 4 - Demo Test Form

After verifying Phase 3:
1. Build test form UI
2. Simulate form operations
3. Test automation engine against demo form
4. Verify field input and save operations
