# Spring Boot with Liquibase Database Migration

This guide will help you set up and use Liquibase with Spring Boot for database schema migrations. Liquibase is a database-independent library for tracking, managing, and applying database schema changes.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Setup](#setup)
  - [Maven Dependencies](#maven-dependencies)
  - [Application Properties](#application-properties)
  - [Liquibase Changelog Structure](#liquibase-changelog-structure)
- [Creating Initial Changelog](#creating-initial-changelog)
- [Running Migrations](#running-migrations)
- [Generating Changesets with Maven Plugin](#generating-changesets-with-maven-plugin)
  - [Maven Plugin Configuration](#maven-plugin-configuration)
  - [Configuring Maven Build Timestamp](#configuring-maven-build-timestamp)
  - [Generating Diff Changesets](#generating-diff-changesets)
- [Rollback Support](#rollback-support)
  - [Adding Rollback Instructions](#adding-rollback-instructions)
  - [Running Rollbacks](#running-rollbacks)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)

## Prerequisites

- Java 17 or higher
- Maven
- PostgreSQL database
- Basic understanding of Spring Boot

## Setup

### Maven Dependencies

Add the following dependencies to your `pom.xml`:

```xml
<dependencies>
    <!-- Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Liquibase -->
    <dependency>
        <groupId>org.liquibase</groupId>
        <artifactId>liquibase-core</artifactId>
    </dependency>
    
    <!-- PostgreSQL Driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

### Application Properties

Configure your database connection and Liquibase in `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://localhost:5432/liquibasedemodb
spring.datasource.username=docker
spring.datasource.password=docker

# JPA Configuration
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.check_nullability=true
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=none

# Liquibase Configuration
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml
```

### Liquibase Changelog Structure

Create the following directory structure:

```
src/main/resources/
└── db/
    └── changelog/
        ├── db.changelog-master.yaml
        └── changelog-1.yaml
```

## Creating Initial Changelog

1. Create a master changelog file `db.changelog-master.yaml`:

```yaml
databaseChangeLog:
  - include:
      file: db/changelog/changelog-1.yaml
```

2. Create your first changelog file `changelog-1.yaml`:

```yaml
databaseChangeLog:
  - changeSet:
      id: 1
      author: developer
      changes:
        - createTable:
            tableName: students
            columns:
              - column:
                  name: id
                  type: BIGINT
                  autoIncrement: true
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: first_name
                  type: VARCHAR(255)
                  constraints:
                    nullable: false
              - column:
                  name: last_name
                  type: VARCHAR(255)
                  constraints:
                    nullable: false
              - column:
                  name: email
                  type: VARCHAR(255)
                  constraints:
                    nullable: false
                    unique: true
```

## Running Migrations

When you start your Spring Boot application, Liquibase will automatically execute the changelog files to update your database schema:

```bash
mvn spring-boot:run
```

Liquibase will:
1. Create two tables: `students` and `databasechangelog`
2. Track all executed changes in the `databasechangelog` table

## Generating Changesets with Maven Plugin

### Maven Plugin Configuration

Add the Liquibase Maven Plugin to your `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.liquibase</groupId>
            <artifactId>liquibase-maven-plugin</artifactId>
            <version>4.31.1</version>
            <configuration>
                <changeLogFile>src/main/resources/db/changelog/db.changelog-master.yaml</changeLogFile>
                <diffChangeLogFile>src/main/resources/db/changelog/${maven.build.timestamp}_changelog.yaml</diffChangeLogFile>
                <driver>org.postgresql.Driver</driver>
                <url>jdbc:postgresql://localhost:5432/liquibasedemodb?createDatabaseIfNotExist=true&amp;allowPublicKeyRetrieval=true&amp;useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8&amp;serverTimezone=UTC</url>
                <defaultSchemaName />
                <username>docker</username>
                <password>docker</password>
                <referenceUrl>hibernate:spring:com.newtonduarte.liquibase.demo2.entities?dialect=org.hibernate.dialect.PostgreSQLDialect
                    &amp;hibernate.physical_naming_strategy=org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy
                    &amp;hibernate.implicit_naming_strategy=org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy
                </referenceUrl>
                <verbose>true</verbose>
            </configuration>
            <dependencies>
                <dependency>
                    <groupId>org.liquibase.ext</groupId>
                    <artifactId>liquibase-hibernate6</artifactId>
                    <version>4.31.1</version>
                </dependency>
                <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-data-jpa</artifactId>
                    <version>3.4.3</version>
                </dependency>
            </dependencies>
        </plugin>
    </plugins>
</build>
```

**Important configuration parameters:**

- `changeLogFile`: Points to the master changelog file
- `diffChangeLogFile`: Specifies where to save the generated changelog, including a timestamp
- `referenceUrl`: Points to your JPA entities package - **update this to match your project structure**

### Configuring Maven Build Timestamp

To customize the format of the timestamp used in generated changelog filenames, add the following properties to your `pom.xml`:

```xml
<properties>
    <maven.build.timestamp.format>yyyyMMddHHmmss</maven.build.timestamp.format>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    <java.version>17</java.version>
</properties>
```

You can customize the timestamp format as needed:

- `yyyyMMddHHmmss` - Basic format (e.g., 20250317125721)
- `yyyy-MM-dd_HH-mm-ss` - More readable format with separators (e.g., 2025-03-17_12-57-21)
- `yyyyMMdd_HHmmss` - Mixed format (e.g., 20250317_125721)

You can also include a descriptive text in your `diffChangeLogFile` path:

```xml
<diffChangeLogFile>src/main/resources/db/changelog/${maven.build.timestamp}_${changeSetDescription}.yaml</diffChangeLogFile>
```

And set the `changeSetDescription` property when running the command:

```bash
mvn liquibase:diff -DchangeSetDescription=add_phone_column
```

This would generate a file like: `20250317125721_add_phone_column.yaml`

### Generating Diff Changesets

After making changes to your JPA entities, generate a diff changelog:

```bash
mvn clean install liquibase:diff -DskipTests=true
```

This will:
1. Compare your JPA entities with the current database schema
2. Generate a new changelog file with the changes needed (e.g., `20250317125721_changelog.yaml`)

After generating the diff, add it to your master changelog:

```yaml
databaseChangeLog:
  - include:
      file: db/changelog/changelog-1.yaml
  - include:
      file: db/changelog/20250317125721_changelog.yaml
```

Run your application again, and Liquibase will apply the new changes.

## Rollback Support

Liquibase provides powerful rollback capabilities that allow you to revert database changes when needed. This is especially useful in CI/CD pipelines and when handling production issues.

### Adding Rollback Instructions

Liquibase can automatically generate rollback instructions for many types of changes, but you can also specify custom rollback instructions in your changelogs:

```yaml
databaseChangeLog:
  - changeSet:
      id: 2
      author: newton
      changes:
        - addColumn:
            tableName: students
            columns:
              - column:
                  name: phone_number
                  type: VARCHAR(20)
      rollback:
        - dropColumn:
            tableName: students
            columnName: phone_number
```

For more complex changes where automatic rollback isn't available, you can specify SQL directly:

```yaml
databaseChangeLog:
  - changeSet:
      id: 3
      author: newton
      changes:
        - sql:
            sql: CREATE INDEX idx_student_name ON students(first_name, last_name)
      rollback:
        - sql:
            sql: DROP INDEX idx_student_name
```

### Running Rollbacks

You can rollback changes using the Liquibase Maven plugin in several ways:

**Rollback by count** - Reverts the last N changesets:

```bash
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```

**Rollback to a specific date** - Reverts all changes after the specified date:

```bash
mvn liquibase:rollback -Dliquibase.rollbackDate=2025-03-15
```

**Rollback to a specific tag** - Reverts all changes after the specified tag:

```bash
mvn liquibase:rollback -Dliquibase.rollbackTag=version_1.0
```

To use tag-based rollbacks, you need to add tags to your changesets:

```yaml
databaseChangeLog:
  - changeSet:
      id: 1
      author: newton
      changes:
        # your changes here
  - tagDatabase:
      tag: version_1.0
  - changeSet:
      id: 2
      author: newton
      changes:
        # more changes here
```

This allows you to rollback to specific application versions or milestones.

## Best Practices

1. **One changeSet per logical change**: Keep your changesets focused on a single logical change.
2. **Never modify existing changesets**: Once a changeset has been applied to any environment, create new changesets for additional changes.
3. **Use descriptive filenames**: Name your changelog files with a timestamp and a brief description of the changes.
4. **Use rollback tags**: Include rollback instructions for each changeset when possible.
5. **Test migrations**: Always test your migrations in a development environment before applying them to production.

## Troubleshooting

If you encounter issues:

1. Check the Liquibase logs for detailed error messages.
2. Verify that your database connection details are correct.
3. Ensure that your JPA entities match the package path in the `referenceUrl`.
4. If using custom naming strategies, make sure they are correctly configured.
5. Check that your database user has sufficient privileges to create/alter tables.

---

Made with ❤️ by Newton Duarte