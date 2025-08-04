# Healing Items Configuration

## JSON Structure

```json
{
  "items": ["minecraft:golden_apple"],
  "is_healing_item": true,
  "cooldown_ticks": 1200,
  "instant_heal": {
    "percent_max_hp": 0.15,
    "percent_missing_hp": 0.1,
    "flat_hp": 2.0
  },
  "heal_over_time": {
    "duration": 60,
    "interval": 20,
    "amount_flat": 1.0,
    "amount_max": 0.02
  }
}
```

## Fields

- **items**: Array of item IDs that this configuration applies to
- **is_healing_item**: If true, prevents multiple healing items from being consumed and triggers cooldown
- **cooldown_ticks**: *(Optional)* Cooldown in ticks before healing items can be used again. If not specified, uses the default from config (600 ticks = 30 seconds)
- **instant_heal**: *(Optional)* Immediate healing when consumed
  - **percent_max_hp**: Heal based on percentage of max health
  - **percent_missing_hp**: Heal based on percentage of missing health  
  - **flat_hp**: Flat healing amount
- **heal_over_time**: *(Optional)* Healing over time effect
  - **duration**: Duration in ticks
  - **interval**: Healing interval in ticks
  - **amount_flat**: Flat healing per interval
  - **amount_max**: Percentage of max health healed per interval

## Examples

### Using Default Cooldown
```json
{
  "items": ["minecraft:apple"],
  "is_healing_item": true,
  "instant_heal": {
    "flat_hp": 1.0
  }
}
```

### Custom Cooldown
```json
{
  "items": ["minecraft:golden_apple"], 
  "is_healing_item": true,
  "cooldown_ticks": 1200,
  "instant_heal": {
    "flat_hp": 4.0
  }
}
```