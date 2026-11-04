# Durable facts for this repository

Facts a future session must not rediscover. Everything here was measured during the
BAH-5156 upgrade (OpenMRS 2.4.2 → 2.8.10, Java 8 → 17); the full evidence lives in
`docs/upgrade/evidence/` on the machine that ran the upgrade (not committed).

## Building and testing

- `./mvnw clean install` is the build command. `clean test-compile` fails on an unmodified
  tree (maven-dependency-plugin MDEP-98: the omod module unpacks the api jar before it is
  packaged). Never treat a `test-compile` failure alone as a regression.
- Java 17 is required to compile (the module targets 17; the artifacts are class-file 61).
  asdf has no default java here: export `JAVA_HOME` explicitly, e.g.
  `/Users/mahekbaru/.asdf/installs/java/openjdk-17`.
- The suite is JUnit 4 tests running under Surefire 3.2.5's JUnit Platform provider with
  Mockito 4.11.0 pinned. 68 tests as of the upgrade (67 baseline + 1 privilege probe).
- The api test database seeds NO privileges; the admin test user passes authorization only
  via the superuser bypass. Any test that wants realistic privilege behavior must create the
  privileges it grants (see `PatientProgramSearchServiceImplGlobalPropertyITTest`).
- `Context.addProxyPrivilege` requires an open OpenMRS session: plain Mockito tests need
  `Context.openSession()`/`closeSession()` around the call (see
  `PatientProgramSearchServiceImplTest`).
- Known-bad test, left alone deliberately: `PatientProgramSearchServiceImplPrivilegeITTest`
  passes vacuously (its users hold zero privileges and its request fixture carries an
  invalid date format). Fixing it is queued as a separate ticket.

## Platform compatibility

- `config.xml`'s `require_version` is filtered from `openmrs.platform.version` (pom) —
  currently 2.8.10. Bumping the platform pom property updates it automatically.
- The `webservices.rest-omod` pin (2.29.0) is a COMPILE-TIME pin only; it deliberately does
  not track the deployed REST module. Runtime compatibility was verified against REST 3.6.0
  (the version the Bahmni-side 2.8 deployments run): full old-vs-new REST surface identical.
- `createCriteria` (Hibernate) is deprecated in this module; it is the known blocker for a
  future platform 3.x / Hibernate 6 move. Do not add new usages.
- OpenMRS 2.8.10's REST contract changed attribute-type payloads: the 2.4.2-era `format`
  field is `datatypeClassname`/`datatypeConfig`/`minOccurs`/`maxOccurs` at runtime.

## Database changelog

- `api/src/main/resources/liquibase.xml` is executed ONLY at deploy time. No test executes
  it — the H2 test schema comes from Hibernate `hbm2ddl.auto=create-drop` from the five
  hbm.xml files. Any changelog change is untested until deployed.
- Its first-ever real execution (Liquibase 4.32, MySQL 8.0) was clean: all 14 changesets
  applied on a fresh database; nothing re-executed on a database where an older module
  version had already run. Verified during the upgrade runtime gate.

## Runtime facts (deployed once on OpenMRS 2.8.x docker hosts)

- The search endpoint's real URL is `/openmrs/ws/rest/v1/episode/search` — it is served by
  the `/ws` DispatcherServlet, NOT under `/module/` (that URL never existed).
- OpenMRS 2.8.10 and 2.8.11-SNAPSHOT (JDK 17) have an upstream platform regression: ANY
  module startup — REST web services alone fails identically — dies in the module-triggered
  Spring refresh with `UnsatisfiedDependencyException: allergyProperties` /
  `NoSuchBeanDefinitionException: AdministrationService @Qualifier("adminService")`.
  It is not caused by this module (proven: 2.8.9 boots the same modules cleanly; the failing
  cache wiring is new, load-bearing, and core-internal). Any runtime verification of this
  module on 2.8.10+ requires the upstream fix; 2.8.9 is the highest host that currently
  boots it. Reproduction matrix and upstream issue draft: `docs/upgrade/evidence/` (machine
  that ran the upgrade).
