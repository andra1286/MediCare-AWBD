# Sistem de gestiune pentru o clinică medicală (MediCare)

## Descrierea proiectului
Aplicația este un sistem complet pentru managementul activității dintr-o clinică medicală, gândit pentru a fi scalabil printr-o arhitectură bazată pe microservicii. Sistemul permite gestionarea utilizatorilor (personal și pacienți), a programărilor medicale, dar și a istoricului medical (fișe de consultație și rețete). 

Trecerea de la un monolit la arhitectura distribuită se va face prin spargerea aplicației în 3 microservicii independente: *Identity Service*, *Appointment Service* și *Medical Records Service*.

## Cerințe funcționale preliminare
1. **Gestiunea utilizatorilor (CRUD):** Crearea, citirea, actualizarea și ștergerea profilurilor pentru medici și pacienți.
2. **Securitate și autorizare:** Autentificare securizată și protejarea endpoint-urilor pe baza a minimum două roluri (ADMIN, DOCTOR, PATIENT).
3. **Gestiunea programărilor:** Pacienții pot vizualiza medicii disponibili și pot rezerva sau anula programări.
4. **Istoric Mmdical și rețete:** Medicii pot crea fișe medicale asociate unei programări finalizate și pot emite rețete conținând mai multe medicamente.
5. **Paginare și sortare:** Navigarea prin listele lungi (ex: lista de programări, lista de pacienți) va fi paginată și sortabilă după minim 2 criterii.

## Modelul de date (ERD)
Modelul nostru de date conține 8 entități interconectate:
* **@OneToOne:** `User` - `DoctorProfile`, `Appointment` - `MedicalRecord`.
* **@OneToMany / @ManyToOne:** `PatientProfile` - `Appointment`, `DoctorProfile` - `Appointment`, `MedicalRecord` - `Prescription`.
* **@ManyToMany:** `User` - `Role`, `Prescription` - `Medication`.

```mermaid
erDiagram
    USER ||--|{ USER_ROLE : has
    ROLE ||--|{ USER_ROLE : granted_to
    USER ||--o| DOCTOR_PROFILE : is_a
    USER ||--o| PATIENT_PROFILE : is_a
    
    DOCTOR_PROFILE ||--o{ APPOINTMENT : has_scheduled
    PATIENT_PROFILE ||--o{ APPOINTMENT : books
    
    APPOINTMENT ||--o| MEDICAL_RECORD : generates
    MEDICAL_RECORD ||--o{ PRESCRIPTION : includes
    
    PRESCRIPTION }|--|{ MEDICATION : contains
