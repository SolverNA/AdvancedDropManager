# AdvancedDropManager

**Paper 1.21.4** плагин для полной подмены ванильного дропа блоков и мобов с гибкими настройками через YAML-конфигурацию.

---

## Возможности

- **Полная замена ванильного дропа** для любых блоков и мобов
- **Два режима расчёта дропа:**
  - `WEIGHTED` — суммируются веса, выпадает ровно один предмет из списка
  - `INDEPENDENT` — каждый предмет проверяется отдельно по своему шансу
- **Гибкая система Fortune/Looting:**
  - Индивидуальное влияние зачарования на каждый предмет
  - Настраиваемый коэффициент (`fortune-factor`)
  - Выбор что увеличивает удача: шанс, количество или оба (`fortune-affects`)
- **Два формата количества:**
  - Диапазон: `amount: "1-5"` (равные шансы)
  - Взвешенное количество: `drop-count` с весами
- **Система провайдеров** — задел на интеграцию с кастомными предметами (ItemsAdder и др.)
- **MiniMessage** для кастомных имён предметов
- **Silk Touch** — автоматический обход кастомного дропа
- **Горячая перезагрузка** конфигурации командой `/adm reload`

---

## Установка

1. Скомпилируйте плагин: `mvn clean package`
2. Скопируйте `target/AdvancedDropManager-1.0-SNAPSHOT.jar` в папку `plugins/` вашего Paper-сервера
3. Запустите/перезапустите сервер
4. Настройте `plugins/AdvancedDropManager/config.yml` под свои нужды
5. Примените изменения: `/adm reload`

---

## Команды

| Команда | Описание | Право |
|---------|----------|-------|
| `/adm` | Информация о плагине | `advanceddropmanager.use` |
| `/adm reload` | Перезагрузка конфигурации | `advanceddropmanager.reload` |

---

## Конфигурация

### Структура

```yaml
blocks:          # Секция блоков
  MATERIAL_NAME: # Ключ — Material блока
    replace-default: true/false  # Заменять ванильный дроп
    roll-type: WEIGHTED/INDEPENDENT  # Режим расчёта
    loot:        # Список предметов
      - id: "unique_id"
        provider: MINECRAFT
        material: DIAMOND
        chance: 75.0          # Шанс в % (для INDEPENDENT)
        weight: 10.0          # Вес (для WEIGHTED)
        fortune: true         # Влияние удачи
        fortune-factor: 1.5   # Коэффициент удачи
        fortune-affects: CHANCE/AMOUNT/BOTH
        amount: "1-3"         # Диапазон количества
        drop-count:           # ИЛИ взвешенное количество
          1: 90
          2: 10
        display-name: "<gold>Имя предмета</gold>"

mobs:            # Секция мобов (аналогично блокам)
  ENTITY_TYPE:   # Ключ — EntityType моба
    ...
```

### Режимы расчёта

#### WEIGHTED (Один из списка)

Система суммирует `weight` всех предметов и выбирает ровно один. Параметр `fortune: true` может быть только у **одного** предмета в группе — если удача срабатывает, выпадает этот предмет; если нет — стандартный расчёт весов.

#### INDEPENDENT (Каждый сам за себя)

Для каждого предмета бросается отдельная кость на `chance`. Параметр `fortune` может быть включён у любого количества предметов.

### Формула удачи

```
Итоговое значение = Base × (1 + Level × Factor)
```

Где:
- `Base` — базовый шанс или количество
- `Level` — уровень зачарования (Fortune/Looting)
- `Factor` — значение `fortune-factor`

### Примеры

**Алмазная руда с увеличенным шансом редкого дропа:**
```yaml
blocks:
  DIAMOND_ORE:
    replace-default: true
    roll-type: INDEPENDENT
    loot:
      - id: "rare_diamond"
        provider: MINECRAFT
        material: DIAMOND
        chance: 75.0
        fortune: true
        fortune-factor: 1.5
        fortune-affects: CHANCE
        drop-count:
          1: 90
          2: 10
        display-name: "<gold>Редкий Алмаз</gold>"
```

**Зомби с Looting-зависимым дропом:**
```yaml
mobs:
  ZOMBIE:
    replace-default: true
    roll-type: INDEPENDENT
    loot:
      - id: "rotten_flesh_drop"
        provider: MINECRAFT
        material: ROTTEN_FLESH
        chance: 90.0
        fortune: true
        fortune-factor: 0.5
        fortune-affects: AMOUNT
        amount: "1-3"
```

---

## Архитектура

```
dev.solverna.advanceddropmanager/
├── AdvancedDropManager.java      # Главный класс плагина
├── command/
│   └── AdvancedDropCommand.java  # Команда /adm
├── config/
│   └── ConfigLoader.java        # Загрузка и парсинг YAML
├── engine/
│   └── DropEngine.java          # Движок расчёта дропа
├── listener/
│   ├── BlockDropListener.java   # Слушатель BlockBreakEvent
│   └── MobDropListener.java     # Слушатель EntityDeathEvent
├── model/
│   ├── FortuneAffects.java      # Enum: CHANCE, AMOUNT, BOTH
│   ├── LootItem.java            # Модель предмета
│   ├── LootTable.java           # Таблица лута
│   └── RollType.java            # Enum: WEIGHTED, INDEPENDENT
└── provider/
    ├── ItemProvider.java         # Интерфейс провайдера
    ├── MinecraftItemProvider.java # Ванильный провайдер
    └── ProviderRegistry.java     # Реестр провайдеров
```

### Расширяемость

Для добавления поддержки кастомных предметов из других плагинов:

1. Реализуйте интерфейс `ItemProvider`
2. Зарегистрируйте провайдер в `ProviderRegistry`
3. Используйте имя провайдера в поле `provider` конфигурации

---

## Требования

- **Java** 21+
- **Paper** 1.21.4
- **Maven** для сборки

---

## Лицензия

Проект разработан для Solverna.
