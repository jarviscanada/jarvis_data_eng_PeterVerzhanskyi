cat > README.md << EOF
# Introduction

# SQL Quries

###### Table Setup (DDL)

###### Insert

```sql
INSERT into cd.facilities
    (facid, name, guestcost, initialoutlay, monthlymaintenance)
    VALUE
    (9, 'Spa', 20, 30, 100000, 800);
```

###### Insert with Select

```sql
INSERT into cd.facilities
    (facid, name, membercost, guestcost, initialoutlay, monthlymaintenance)
    VALUE
    ((SELECT MAX(facid) FROM cd.facilities) + 1, 'Spa', 20, 30, 100000, 800);
```

###### Update
```sql
UPDATE cd.facilities
SET initialoutlay = 10000
WHERE name = 'Tennis Court 2';
```

###### Update with Subquery
```sql
UPDATE cd.facilities
SET membercost = (SELECT membercost * 1.1 FROM cd.facilities WHERE facid = 0),
    guestcost = (SELECT guestcost * 1.1 FROM cd.facilities WHERE facid = 0)
WHERE name = 'Tennis Court 2';
```
###### Delete All Bookings
```sql
DELETE FROM cd.bookings;
```

###### Delete Specific Member
```sql
DELETE FROM cd.members WHERE memid = 37;
```

###### Select with Conditional Logic
```sql
SELECT faceid, name, membercost, monthlymaintenance
FROM cd.facilities
WHERE monthlymaintenance > 50 * membercost AND membercost != 0;
```
###### Select with LIKE
```sql
SELECT *
FROM cd.facilities
WHERE name LIKE '%Tennis%';
```

###### Select with IN
```sql
SELECT *
FROM cd.facilities
WHERE facid IN (1,5);
```

###### Select with Members After Date
```sql
SELECT memid,surname, firstname, joindate
FROM cd.members
WHERE joindate > '2012-09-01';
```

###### Union Query
```sql
SELECT surnam AS name
FROM cd.members
UNION
SELECT name AS name
FROM cd.facilities
```

###### Join to Find Booking Times
```sql
SELECT b.starttime
FROM cd.bookings b
JOIN cd.members m ON b.memid = m.memid
WHERE m.firstname = 'David' AND m.surname = 'Farrell';
```

###### Join with Date Filtering
```sql
SELECT b.starttime, f.name
FROM cd.bookings b
JOIN cd.facilities f ON b.facid = f.facid
WHERE f.name LIKE 'Tennis Court%'
AND b.starttime::date = '2012-09-21'
ORDER BY b.starttime;
```

###### Self Join
```sql
SELECT mems.firstname AS memfname,
       mems.surname AS memsname,
       recs.firstname AS recfname,
       recs.surname AS recsname
FROM cd.members mems
         LEFT OUTER JOIN cd.members recs ON recs.memid = mems.recommendedby
ORDER BY memsname, memfname;
```

###### Self Join to Get Recommendations
```sql
SELECT DISTINCT recs.firstname AS firstname,
                recs.surname AS surname
FROM cd.members mems
INNER JOIN cd.members recs ON recs.memid = mems.recommendedby
ORDER BY surname, firstname;
```

###### Select with Subquery
```sql
SELECT DISTINCT
    m.firstname || ' ' || m.surname AS member_name,
    (SELECT r.firstname || ' ' || r.surname
    FROM cd.members r
    WHERE r.memid = m.recommendedby) AS recommender_name
FROM cd.members m
ORDER BY member_name;
```

###### Aggregation Query
```sql
SELECT recommendedby AS memid, COUNT(*) AS recommendation_count
FROM cd.members
WHERE recommendedby IS NOT NULL
GROUP BY recommendedby
ORDER BY memeid;
```

###### Sum of Slots
```sql
SELECT facid, SUM(slots) AS total_slots
FROM cd.bookings
GROUP by facid
ORDER BY facid;
```

###### Sum of slots with Date Filter
```sql
SELECT facid, SUM(slots) AS "Total Slots"
FROM cd.bookings
WHERE starttime >= '2012-09-01'
  AND starttime < '2012-10-01'
GROUP BY facid
ORDER BY SUM(slots);
```

###### Monthly slot totals for 2012
```sql
SELECT facid,
       EXTRACT(month FROM starttime) AS month,
       SUM(slots) AS "Total Slots"
FROM cd.bookings
WHERE EXTRACT(yesr FROM starttime) = 2012
GROUP BY facid, month
ORDER BY facid, month;
```

###### Count Unique Members who made bookings
```sql
SELECT COUNT(DISTINCT memid) AS total_members
FROM cd.bookings;
```

###### Earliest Booking after September 1, 2012
```sql
SELECT mems.surname,
       mems.firstname,
       mems.memid,
       MIN(bks.starttime) AS starttime
FROM cd.bookings bks
INNER JOIN cd.members mems ON mems.memid = bks.memid
WHERE bks.starttime >= '2012-09-01'
GROUP BY mems.surname, mems.firstname, mems.surname, mems.firstname, mems.memid
ORDER BY mems.memid;
```

###### Count of Members Ordered by Join Date
```sql
SELECT count(*) over (), firstname, surname
    FROM cd.members
ORDER BY joindate;
```

###### Row Number for Members Ordered by Join Date
```sql
SELECT ROW_NUMBER() OVER (ORDER BY joindate) AS member_number
        firstname,
        surname
FROM cd.members
ORDER BY joindate;
```

###### Highest Total Slots Booked
```sql
SELECT facid, total
FROM (
    SELECT facid,
           SUM(slots) AS total,
           RANK() OVER (ORDER BY SUM(slots) DECS) AS rank
    FROM cd.bookings
    GROUP BY facid
     ) AS ranked
WHERE rank = 1;
```

###### Format Member Names
```sql
SELECT CONCAT(surname, ', ', firstname) AS formatted_name
FROM cd.members;
```

###### Select Members with Parentheses in phone number
```sql
SELECT memid, telephone
FROM cd.members
WHERE telephone LIKE '%(%' OR telephone LIKE '%)%'
ORDER BY memid;
```

###### Count Members by First Letter of Surname
```sql
SELECT LEFT(surname, 1) AS first_letter, COUNT(*) AS member_count
FROM cd.members
GROUP BY first_letter
ORDER BY first_letter;
```

EOF
