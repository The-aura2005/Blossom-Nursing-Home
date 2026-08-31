function showSection(sectionId,button){
    document.querySelectorAll('.tab').forEach(function(section){
        section.style.display = 'none';
    });

    document.querySelectorAll('.tab').forEach(function(tab){
        tab.classList.remove('active');
    });
    document.getElementById(sectionId).style.display = "block";
    button.classList.add('active');

    document.addEventListener('DOMContentLoaded', function(){
        const firstTab = document.querySelector('.tab');
        showSection('vitals',firstTab);

    });

}