window.onload = init;

function init() {
    displayHttpGet("/api/records");
    document.getElementById('filterForm').addEventListener('submit', (event) => {
        event.preventDefault();
        applyFilters(document.getElementById('filterForm').elements);
    });
    setupColumnSorting();
}

function setupColumnSorting() {
    const getCellValue = (tr, idx) => tr.children[idx].dataset.value;

    const comparerFactory = (idx, asc) => (a, b) => {
        const v1 = getCellValue(asc ? a : b, idx);
        const v2 = getCellValue(asc ? b : a, idx);

        const bothNumbers = v1 !== '' && v2 !== '' && !isNaN(v1) && !isNaN(v2);
        return bothNumbers ? v1 - v2 : v1.toString().localeCompare(v2);
    };

    document.querySelectorAll('th').forEach((th, tableHeaderIndex) => th.addEventListener('click', (() => {

        // Reset all sorting arrows on other columns.
        document.querySelectorAll('th').forEach(th => th.removeAttribute('data-sortDirection'));
        // Set the sort direction for the clicked table header.
        this.asc ? th.setAttribute('data-sortDirection', 'asc') : th.setAttribute('data-sortDirection', 'desc');
        const table = th.closest('table');
        const tbody = table.querySelector('tbody');
        Array.from(tbody.querySelectorAll('tr'))
            .sort(comparerFactory(tableHeaderIndex, this.asc = !this.asc))
            .forEach(tr => tbody.appendChild(tr) );
    })));
}

function applyFilters(filters) {
    let queries = [];
    if (filters.commitHash.value.length > 0) {
        queries.push("commitHash=" + filters.commitHash.value);
    }
    if (filters.majorVersion.value.length > 0) {
        queries.push("majorVersion=" + filters.majorVersion.value);
    }
    if (filters.minorVersion.value.length > 0) {
        queries.push("minorVersion=" + filters.minorVersion.value);
    }
    if (filters.patchVersion.value.length > 0) {
        queries.push("patchVersion=" + filters.patchVersion.value);
    }
    if (filters.dirty.value.length > 0) {
        const dirtyVal = filters.dirty.value;
        if (dirtyVal == "dirty") {
            queries.push("dirty=" + "true");
        } else {
            queries.push("dirty=" + "false");
        }
    }
    if (filters.timestampFirst.value.length > 0) {
        const date = new Date(filters.timestampFirst.value);
        queries.push("timestampFirst=" + date.getTime());
    }
    if (filters.timestampLast.value.length > 0) {
        const date = new Date(filters.timestampLast.value);
        queries.push("timestampLast=" + date.getTime());
    }
    console.log(filters.commitHash.value);
    console.log(filters.majorVersion.value);
    console.log(filters.minorVersion.value);
    console.log(filters.patchVersion.value);
    console.log(filters.dirty.value);
    console.log(filters.timestampLast.value);
    console.log(filters.timestampFirst.value);

    let url = "/api/records";
    if (queries.length > 0) {
        url += "?" + queries.join('&');
    }
    console.log(url);
    displayHttpGet(url);
}

function displayHttpGet(url) {

    // Reset all sorting arrows on other columns.
    document.querySelectorAll('th').forEach(th => th.removeAttribute('data-sortDirection'));
    const table = document.getElementById("recordsTable");
    const newBody = document.createElement("tbody");
    const oldBody = table.getElementsByTagName('tbody')[0];

    fetch(url)
        .then(response => response.json())
        .then(records => {
            for (const record of records) {
                // Combine the versions together.
                const version = `${record.majorVersion}.${record.minorVersion}.${record.patchVersion}`;

                row = newBody.insertRow();

                // Version cell.
                row.insertCell(0).appendChild(document.createTextNode(version));
                // Assumes major and minor are 1 byte each and patch is 2 bytes (same assumption made in ble_iot).
                // Value is composed using bit shifts to allow sorting values lexicographically.
                row.children[0].dataset.value = record.majorVersion << 24 | record.minorVersion << 16 | record.patchVersion;

                // Commit hash cell.
                row.insertCell(1).appendChild(document.createTextNode(record.commitHash));
                row.children[1].appendChild(renderDirtyIconWithTooltip(record.dirty));
                row.children[1].dataset.value = record.commitHash;

                // Timestamp cell.
                row.insertCell(2).appendChild(renderTimestampWithTooltip(record.timestamp));
                row.children[2].dataset.value = Date.parse(record.timestamp);
            }

        });

    table.replaceChild(newBody, oldBody);
}

function renderDirtyIconWithTooltip(isDirty) {

    const iconType = isDirty ? "dirty-icon" : "clean-icon";
    const icon = isDirty ? "warning" : "check_circle";
    const tooltipText = isDirty ? "Dirty" : "Clean";

    const dirtyIconContainer = document.createElement('i');
    dirtyIconContainer.className = "material-icons " + iconType;
    dirtyIconContainer.appendChild(document.createTextNode(icon));

    const tooltipTextContainer = document.createElement("span");
    tooltipTextContainer.className = "tooltipText";
    tooltipTextContainer.appendChild(document.createTextNode(tooltipText));

    const div = document.createElement('div');
    div.className = "tooltip";
    div.style.float = "right";
    div.appendChild(dirtyIconContainer);
    div.appendChild(tooltipTextContainer);

    return div;
}

// Given string formatted with ISO-8601.
function renderTimestampWithTooltip(timestamp) {

    const date = new Date(timestamp);
    // Uses day-month-year order and 24-hour time without AM/PM.
    const formattedDate = date.toLocaleString();

    const tooltipTextContainer = document.createElement("span");
    tooltipTextContainer.className = "tooltipText";
    tooltipTextContainer.appendChild(document.createTextNode(formattedDate));

    const div = document.createElement('div');
    div.className = "tooltip";
    div.appendChild(document.createTextNode(timeago.format(timestamp)));
    div.appendChild(tooltipTextContainer);

    return div;
}