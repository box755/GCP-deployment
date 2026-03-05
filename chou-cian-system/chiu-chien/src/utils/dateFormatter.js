export function formatDate(dateString) {
    if (!dateString) return '';

    const date = new Date(dateString);

    const options = {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    };

    return new Intl.DateTimeFormat('en-US', options).format(date);
}
