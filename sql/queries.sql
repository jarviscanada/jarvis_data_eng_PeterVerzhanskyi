-- MODIFYING DATA
-- 1.
INSERT into cd.facilities
    (facid, name, guestcost, initialoutlay, monthlymaintenance)
VALUE
    (9, 'Spa', 20, 30, 100000, 800);

-- 2

INSERT into cd.facilities
    (facid, name, membercost, guestcost, initialoutlay, monthlymaintenance)
VALUE
    ((SELECT MAX(facid) FROM cd.facilities) + 1, 'Spa', 20, 30, 100000, 800);

-- 3
UPDATE cd.facilities
SET initialoutlay = 10000
WHERE name = 'Tennis Court 2';

-- 4
UPDATE cd.facilities
SET membercost = (SELECT membercost * 1.1 FROM cd.facilities WHERE facid = 0),
    guestcost = (SELECT guestcost * 1.1 FROM cd.facilities WHERE facid = 0)
WHERE name = 'Tennis Court 2';

-- 5
DELETE FROM cd.bookings;

-- 6
DELETE FROM cd.members WHERE memid = 37;

-- BASICS
-- 7
SELECT faceid, name, membercost, monthlymaintenance
FROM cd.facilities
WHERE monthlymaintenance > 50 * membercost AND membercost != 0;

--8
SELECT *
FROM cd.facilities
WHERE name LIKE '%Tennis%';

--9
SELECT *
FROM cd.facilities
WHERE facid IN (1,5);

--10
SELECT memid,surname, firstname, joindate
FROM cd.members
WHERE joindate > '2012-09-01';

--11
SELECT surnam AS name
FROM cd.members
UNION
SELECT name AS name
FROM cd.facilities

-- JOIN
-- 12
SELECT b.starttime
FROM cd.bookings b
JOIN cd.members m ON b.memid = m.memid
WHERE m.firstname = 'David' AND m.surname = 'Farrell';

--13
SELECT b.starttime, f.name
FROM cd.bookings b
JOIN cd.facilities f ON b.facid = f.facid
WHERE f.name LIKE 'Tennis Court%'
AND b.starttime::date = '2012-09-21'
ORDER BY b.starttime;

--14
SELECT mems.firstname AS memfname,
       mems.surname AS memsname,
       recs.firstname AS recfname,
       recs.surname AS recsname
FROM cd.members mems
         LEFT OUTER JOIN cd.members recs ON recs.memid = mems.recommendedby
ORDER BY memsname, memfname;

--15

SELECT DISTINCT recs.firstname AS firstname,
                recs.surname AS surname
FROM cd.members mems
INNER JOIN cd.members recs ON recs.memid = mems.recommendedby
ORDER BY surname, firstname;

-- 16
SELECT DISTINCT
    m.firstname || ' ' || m.surname AS member_name,
    (SELECT r.firstname || ' ' || r.surname
    FROM cd.members r
    WHERE r.memid = m.recommendedby) AS recommender_name
FROM cd.members m
ORDER BY member_name;

-- Aggregation
--17

SELECT recommendedby AS memid, COUNT(*) AS recommendation_count
FROM cd.members
WHERE recommendedby IS NOT NULL
GROUP BY recommendedby
ORDER BY memeid;

--18
SELECT facid, SUM(slots) AS total_slots
FROM cd.bookings
GROUP by facid
ORDER BY facid;

--19
SELECT facid, SUM(slots) AS "Total Slots"
FROM cd.bookings
WHERE starttime >= '2012-09-01'
  AND starttime < '2012-10-01'
GROUP BY facid
ORDER BY SUM(slots);

--20
SELECT facid,
       EXTRACT(month FROM starttime) AS month,
       SUM(slots) AS "Total Slots"
FROM cd.bookings
WHERE EXTRACT(yesr FROM starttime) = 2012
GROUP BY facid, month
ORDER BY facid, month;

--21
SELECT COUNT(DISTINCT memid) AS total_members
FROM cd.bookings;

--22
SELECT mems.surname,
       mems.firstname,
       mems.memid,
       MIN(bks.starttime) AS starttime
FROM cd.bookings bks
INNER JOIN cd.members mems ON mems.memid = bks.memid
WHERE bks.starttime >= '2012-09-01'
GROUP BY mems.surname, mems.firstname, mems.surname, mems.firstname, mems.memid
ORDER BY mems.memid;

--23

SELECT count(*) over (), firstname, surname
    FROM cd.members
ORDER BY joindate;

--24
SELECT ROW_NUMBER() OVER (ORDER BY joindate) AS member_number
        firstname,
        surname
FROM cd.members
ORDER BY joindate;

--25
SELECT facid, total
FROM (
    SELECT facid,
           SUM(slots) AS total,
           RANK() OVER (ORDER BY SUM(slots) DECS) AS rank
    FROM cd.bookings
    GROUP BY facid
     ) AS ranked
WHERE rank = 1;

-- STRING
-- 26
SELECT CONCAT(surname, ', ', firstname) AS formatted_name
FROM cd.members;

-- 27
SELECT memid, telephone
FROM cd.members
WHERE telephone LIKE '%(%' OR telephone LIKE '%)%'
ORDER BY memid;

--28
SELECT LEFT(surname, 1) AS first_letter, COUNT(*) AS member_count
FROM cd.members
GROUP BY first_letter
ORDER BY first_letter;





