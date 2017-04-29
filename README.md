# Веб-версия клиники домашних питомцев

Мой вольный вариант веб-версии программы **"Клиника домашних питомцев"** из курса Петра Арсентьева
**"Java. Путь от ученика до эксперта"**.

Ссылка на сам курс: http://job4j.ru/courses/java_way_from_student_to_master.html

Для запуска нужно создать war-файл командой `mvn package`, а затем развернуть его на сервере *Apache Tomcat*.

Следует иметь ввиду, что сборка данного проекта зависит от базового проекта **"Клиника домашних питомцев"**, который
предварительно должен быть добавлен в локальный репозиторий maven командой `mvn install`.

Также для работы данного проекта необходима рабочая база данных MySQL, параметры которой настраиваются в файле
`src/main/resources/jdbc.properties`, а требуемая схема описана в `sql/clinic.sql`.

**Список клиентов с питомцами:**

![Screenshot](img/screenshot.png)

**Добавление нового клиента:**

![Screenshot](img/screenshot2.png)

**Установка питомца клиенту:**

![Screenshot](img/screenshot3.png)

**Обновление параметров клиента:**

![Screenshot](img/screenshot4.png)

**Обновление параметров питомца:**

![Screenshot](img/screenshot5.png)

**Список всех питомцев:**

![Screenshot](img/screenshot6.png)

**Лекарства в клинике:**

![Screenshot](img/screenshot7.png)

**Выдача лекарства питомцу клиента:**

![Screenshot](img/screenshot8.png)

**Роли:**

![Screenshot](img/screenshot9.png)

**Сообщения:**

![Screenshot](img/screenshot10.png)
